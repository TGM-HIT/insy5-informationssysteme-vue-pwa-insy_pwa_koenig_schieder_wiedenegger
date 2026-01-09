<template>
  <div v-if="show" class="modal-overlay" @click.self="$emit('close')" data-cy="add-modal">
    <div class="modal-container">
      <div class="modal-header">
        <h2>New {{ entityType }}</h2>
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
                :step="getInputStep(field)"
                class="form-input"
            />
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="$emit('close')" data-cy="modal-cancel-btn">
              Cancel
            </button>
            <button type="submit" class="btn btn-primary" data-cy="modal-submit-btn">
              Create
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
    entityType: String,
    fields: Array
  },
  emits: ['close', 'add'],
  data() {
    return {
      formData: {}
    }
  },
  computed: {
    editableFields() {
      // Show all fields except for ones that are auto-generated
      return this.fields.filter(field =>
          ![
            'date_exported',
            'a_id',
            'id',
            'dateCreated',
            'dateExported'
          ].includes(field)
      );
    }
  },
  watch: {
    show(newVal) {
      if (newVal) {
        this.formData = {}
      }
    }
  },
  methods: {
    handleSubmit() {
      const dataToSave = { ...this.formData };

      // Ensure timestamps include seconds
      this.editableFields.forEach(field => {
        if (this.isDateField(field) && dataToSave[field]) {
          let dt = dataToSave[field];
          if (dt.length === 16) { // Format is YYYY-MM-DDTHH:mm
            dataToSave[field] = dt + ':00';
          }
        }
      });

      // Handle numeric conversions
      for (const field in dataToSave) {
        if (this.isNumericField(field)) {
          const value = dataToSave[field];
          if (value !== null && value !== undefined && value !== '') {
            const num = Number(value);
            if (!isNaN(num)) {
              dataToSave[field] = num;
            }
          }
        }
      }

      // Special handling for composite keys
      if (this.entityType === 'boxpos') {
        this.$emit('add', {
          bpos_id: dataToSave.bpos_id,
          b_id: dataToSave.b_id,
          s_id: dataToSave.s_id,
          s_stamp: dataToSave.s_stamp
        });
      } else if (this.entityType === 'sample') {
        const { s_id, s_stamp, ...rest } = dataToSave;
        this.$emit('add', {
          id: {
            s_id: s_id,
            s_stamp: s_stamp
          },
          ...rest
        });
      } else {
        this.$emit('add', dataToSave);
      }

      this.formData = {};
    },

    formatLabel(field) {
      return field
          .split('_')
          .map(word => word.charAt(0).toUpperCase() + word.slice(1))
          .join(' ');
    },

    isDateField(field) {
      return field.includes('date') || field.includes('stamp');
    },

    isTextAreaField(field) {
      return field === 'comment' || field === 'info';
    },

    isNumericField(field) {
      const numericFields = [
        'num_max', 'type', 'quantity', 'distance',
        'weight_net', 'weight_bru', 'weight_tar',
        'pol', 'nat', 'kal', 'an', 'glu', 'dry',
        'bpos_id'
      ];
      return numericFields.includes(field);
    },

    getInputType(field) {
      if (this.isNumericField(field)) {
        return 'number';
      }
      if (field === 'b_id' || field === 's_id') {
        return 'text';
      }
      return 'text';
    },

    getInputStep(field) {
      if (field.includes('weight') || field.includes('density')) {
        return '0.01';
      }
      return '1';
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
