<template>
  <div class="column-selector">
    <button @click="showDropdown = !showDropdown" class="btn-columns" data-cy="btn-column-selector">
      <span>⚙️</span> Columns
    </button>

    <div v-if="showDropdown" class="dropdown-panel" data-cy="column-dropdown">
      <div class="dropdown-header">
        <h3>Select Columns</h3>
        <button @click="showDropdown = false" class="btn-close">✕</button>
      </div>

      <div class="column-list">
        <label
            v-for="col in allColumns"
            :key="col"
            class="column-item"
            :data-cy="`column-toggle-${col}`"
        >
          <input
              type="checkbox"
              :checked="visibility[col]"
              @change="$emit('toggle', col)"
              :disabled="visibleCount === 1 && visibility[col]"
          />
          <span>{{ formatHeader(col) }}</span>
        </label>
      </div>

      <div class="dropdown-footer">
        <button @click="selectAll" class="btn-select-all">Select All</button>
        <button @click="resetDefaults" class="btn-reset">Reset</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ColumnSelector',
  props: {
    allColumns: Array,
    visibility: Object
  },
  data() {
    return {
      showDropdown: false
    }
  },
  computed: {
    visibleCount() {
      return Object.values(this.visibility).filter(v => v).length
    }
  },
  methods: {
    formatHeader(col) {
      const headerMap = {
        'a_id': 'ID',
        's_id': 'Sample ID',
        's_stamp': 'Timestamp',
        'pol': 'POL',
        'nat': 'NAT',
        'kal': 'KAL',
        'an': 'AN',
        'glu': 'GLU',
        'dry': 'DRY',
        'date_in': 'Date In',
        'date_out': 'Date Out',
        'weight_mea': 'Weight Mea.',
        'weight_nrm': 'Weight Nrm.',
        'weight_cur': 'Weight Cur.',
        'weight_dif': 'Weight Dif.',
        'weight_net': 'Weight Net',
        'weight_bru': 'Weight Bru.',
        'weight_tar': 'Weight Tar',
        'density': 'Density',
        'a_flags': 'Flags',
        's_flags': 'Flags',
        'lane': 'Lane',
        'comment': 'Comment',
        'date_exported': 'Exported',
        'name': 'Name',
        'quantity': 'Quantity',
        'distance': 'Distance',
        'date_crumbled': 'Crumbled',
        'b_id': 'Box ID',
        'bpos_id': 'Position ID',
        'num_max': 'Max Number',
        'type': 'Type',
        'log_id': 'Log ID',
        'date_created': 'Created',
        'level': 'Level',
        'info': 'Info'
      }
      return headerMap[col] || col.toUpperCase()
    },
    selectAll() {
      this.$emit('select-all')
    },
    resetDefaults() {
      this.$emit('reset')
    }
  }
}
</script>

<style scoped>
.column-selector {
  position: relative;
  display: inline-block;
}

.btn-columns {
  background: #6b7280;
  color: white;
  border: none;
  padding: 11px 22px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-columns:hover {
  background: #4b5563;
  transform: translateY(-2px);
}

.dropdown-panel {
  position: absolute;
  top: 50px;
  right: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
  width: 280px;
  z-index: 1000;
  max-height: 500px;
  display: flex;
  flex-direction: column;
}

.dropdown-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.dropdown-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.btn-close {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #6b7280;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-close:hover {
  color: #1f2937;
}

.column-list {
  padding: 12px;
  overflow-y: auto;
  max-height: 350px;
}

.column-item {
  display: flex;
  align-items: center;
  padding: 8px;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.15s;
}

.column-item:hover {
  background: #f3f4f6;
}

.column-item input[type="checkbox"] {
  margin-right: 10px;
  cursor: pointer;
}

.column-item input[type="checkbox"]:disabled {
  cursor: not-allowed;
}

.dropdown-footer {
  display: flex;
  gap: 8px;
  padding: 12px;
  border-top: 1px solid #e5e7eb;
}

.btn-select-all,
.btn-reset {
  flex: 1;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn-select-all {
  background: #3b82f6;
  color: white;
}

.btn-select-all:hover {
  background: #2563eb;
}

.btn-reset {
  background: #e5e7eb;
  color: #374151;
}

.btn-reset:hover {
  background: #d1d5db;
}
</style>
