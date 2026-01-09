<template>
  <div v-if="show" class="modal-overlay" @click.self="$emit('close')" data-cy="edit-modal">
    <div class="modal-container">
      <div class="modal-header">
        <h2>Edit {{ entityType }}</h2>
        <button class="close-btn" @click="$emit('close')" data-cy="modal-close-btn">✕</button>
      </div>

      <div class="modal-body">
        <form @submit.prevent="handleSubmit">
          <div v-for="field in editableFields" :key="field" class="form-group">
            <label :for="field">{{ formatLabel(field) }}</label>
            <input
                v-if="isDateField(field)"
                :id="field"
                :data-cy="`input-${field}`"
                v-model="formData[field]"
                type="datetime-local"
                class="form-input"
                step="1"
            />
            <textarea
                v-else-if="isTextAreaField(field)"
                :id="field"
                :data-cy="`input-${field}`"
                v-model="formData[field]"
                class="form-textarea"
                rows="3"
            />
            <input
                v-else
                :id="field"
                :data-cy="`input-${field}`"
                v-model="formData[field]"
                :type="getInputType(field)"
                :inputmode="isNumericField(field) ? 'decimal' : undefined"
                class="form-input"
            />
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="$emit('close')" data-cy="modal-cancel-btn">
              Cancel
            </button>
            <button type="submit" class="btn btn-primary" data-cy="modal-submit-btn">
              Save Changes
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    show: Boolean,
    item: Object,
    entityType: String,
    fields: Array
  },
  emits: ['close', 'save'],
  data() {
    return {
      formData: {}
    }
  },
  computed: {
    editableFields() {
      const genericExclusions = new Set(['date_exported', 'log_id']);

      const perEntityExclusions = {
        analysis: new Set(['a_id', 's_id', 's_stamp']),
        sample: new Set(['s_id', 's_stamp']),
        box: new Set(['b_id']),
        boxpos: new Set([])
      };

      const entity = (this.entityType || '').toLowerCase();
      const entitySet = perEntityExclusions[entity] || new Set();

      return this.fields.filter(field => {
        if (genericExclusions.has(field)) return false;
        if (entitySet.has(field)) return false;
        return true;
      });
    }
  },
  watch: {
    item: {
      immediate: true,
      handler(newItem) {
        if (newItem) {
          this.formData = { ...newItem }
          this.editableFields.forEach(field => {
            if (this.isDateField(field) && this.formData[field]) {
              this.formData[field] = this.formatDateForInput(this.formData[field])
            }
          })
        }
      }
    }
  },
  methods: {
    handleSubmit() {
      let dataToSave = { ...this.formData };

      // Ensure timestamps include seconds
      this.editableFields.forEach(field => {
        if (this.isDateField(field) && dataToSave[field]) {
          let dt = dataToSave[field];
          if (dt.length === 16) { // Format is YYYY-MM-DDTHH:mm
            dataToSave[field] = dt + ':00';
          }
        }
      });

      this.editableFields.forEach(field => {
        const val = dataToSave[field];
        if (this.isNumericField(field) && val !== undefined && val !== null && val !== '') {
          const normalized = String(val).replace(',', '.');
          const num = Number(normalized);
          if (!Number.isNaN(num)) {
            dataToSave[field] = num;
          } else {
            dataToSave[field] = null;
          }
        }
      });

      if (this.entityType === 'boxpos') {
        dataToSave = {
          id: {
            bposId: dataToSave.bpos_id,
            bId: dataToSave.b_id
          },
          sampleId: dataToSave.s_id,
          sampleStamp: dataToSave.s_stamp,
          dateExported: dataToSave.date_exported
        };
      }

      this.$emit('save', dataToSave);
    },
    formatLabel(field) {
      return field
          .split('_')
          .map(word => word.charAt(0).toUpperCase() + word.slice(1))
          .join(' ');
    },
    isDateField(field) {
      return field.includes('date') || field.includes('stamp') || field === 's_stamp';
    },
    isTextAreaField(field) {
      return field === 'comment' || field === 'info';
    },
    getInputType(field) {
      if (this.isNumericField(field)) {
        return 'text';
      }
      return 'text';
    },
    formatDateForInput(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      const seconds = String(date.getSeconds()).padStart(2, '0');
      return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
    },
    isNumericField(field) {
      if (field.includes('weight') || field.includes('density')) return true;
      if (['quantity', 'distance', 'num_max', 'pol', 'nat', 'kal', 'an', 'glu', 'dry', 'weight_mea', 'weight_nrm', 'weight_cur', 'weight_dif'].includes(field)) return true;
      return false;
    }
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-container {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 85vh;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(30px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-header {
  padding: 24px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.modal-header h2 {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  cursor: pointer;
  color: white;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  transition: all 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
  max-height: calc(85vh - 140px);
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #374151;
  font-size: 14px;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.2s;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-textarea {
  resize: vertical;
  font-family: inherit;
}

.modal-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
}

.btn {
  padding: 10px 20px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.btn-secondary {
  background: #f3f4f6;
  color: #374151;
}

.btn-secondary:hover {
  background: #e5e7eb;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}
</style>
