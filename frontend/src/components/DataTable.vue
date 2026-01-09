<template>
  <div class="table-container">
    <div class="header">
      <h2>{{ title }}</h2>
      <button
          v-if="title.toLowerCase() !== 'log'"
          @click="$emit('add')"
          class="btn-add"
          data-cy="btn-add-new"
      >
        <span>+</span> Add New
      </button>
    </div>

    <div v-if="loading" class="loading" data-cy="loading-state">
      <div class="spinner"></div>
      <p>Loading...</p>
    </div>

    <div v-else-if="data.length === 0" class="no-data" data-cy="no-data-state">
      <p>No data available</p>
    </div>

    <div v-else class="table-wrapper">
      <table data-cy="data-table">
        <thead>
        <tr>
          <th v-if="expandable" class="expand-col"></th>
          <th
              v-for="col in columns"
              :key="col"
              :class="['sortable', sortBy === col ? 'sorted' : '']"
              :aria-sort="ariaSort(col)"
              @click="onHeaderClick(col)"
              :data-cy="`th-${col}`"
          >
            <span class="th-label">{{ formatHeader(col) }}</span>
            <span class="sort-indicator" v-if="sortBy === col">{{ sortDir === 'asc' ? '▲' : '▼' }}</span>
          </th>
          <th class="actions-header">Actions</th>
        </tr>
        </thead>
        <tbody>
        <template v-for="(row, index) in data" :key="index">
          <tr :data-cy="`table-row-${index}`">
            <td v-if="expandable" class="expand-cell">
              <button class="btn-expand" @click="$emit('toggle-expand', row)" :aria-expanded="isExpanded(row)">
                {{ isExpanded(row) ? '−' : '+' }}
              </button>
            </td>
            <td v-for="col in columns" :key="col" :class="getCellClass(row[col])" :data-cy="`cell-${col}`">
              {{ formatValue(row[col]) }}
            </td>
            <td class="actions-cell" v-if="title.toLowerCase() !== 'log'">
              <button @click="$emit('edit', row)" class="btn-edit" data-cy="btn-edit">Edit</button>
              <button @click="$emit('delete', row)" class="btn-delete" data-cy="btn-delete">Delete</button>
            </td>
            <td class="actions-cell" v-else>
              <span class="read-only-label" data-cy="read-only-label">read-only</span>
            </td>
          </tr>
          <tr v-if="expandable && isExpanded(row)" class="expand-row">
            <td :colspan="columns.length + 1 + 1">
              <div class="child-table-wrapper">
                <table class="child-table">
                  <thead>
                  <tr>
                    <th v-for="cc in childColumns" :key="cc">{{ formatHeader(cc) }}</th>
                  </tr>
                  </thead>
                  <tbody>
                  <tr v-for="(child, cidx) in childMap[row[expandKey]] || []" :key="cidx">
                    <td v-for="cc in childColumns" :key="cc">{{ formatValue(child[cc]) }}</td>
                  </tr>
                  <tr v-if="!childMap[row[expandKey]] || (childMap[row[expandKey]] && childMap[row[expandKey]].length === 0)">
                    <td :colspan="childColumns.length" class="no-children">No related entries</td>
                  </tr>
                  </tbody>
                </table>
              </div>
            </td>
          </tr>
        </template>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DataTable',
  props: {
    title: String,
    columns: Array,
    data: Array,
    loading: Boolean,
    sortBy: String,
    sortDir: String,
    // expansion
    expandable: { type: Boolean, default: false },
    expandKey: { type: String, default: null },
    expandedKeys: { type: Array, default: () => [] },
    childMap: { type: Object, default: () => ({}) },
    childColumns: { type: Array, default: () => [] }
  },
  methods: {
    onHeaderClick(col) {
      this.$emit('sort-change', col)
    },
    ariaSort(col) {
      if (this.sortBy !== col) return 'none'
      return this.sortDir === 'asc' ? 'ascending' : 'descending'
    },
    isExpanded(row) {
      if (!this.expandable || !this.expandKey) return false
      const key = row[this.expandKey]
      return this.expandedKeys.includes(key)
    },
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

    formatValue(value) {
      if (value === null || value === undefined || value === '') {
        return '-'
      }

      if (typeof value === 'string') {
        if (value.match(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/)) {
          const date = new Date(value)
          return date.toLocaleString('de-DE', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
          })
        }
        return value
      }

      if (Number.isInteger(value)) {
        return value.toString()
      }

      if (typeof value === 'number') {
        return value.toFixed(2)
      }

      if (typeof value === 'object') {
        return JSON.stringify(value)
      }

      return value.toString()
    },

    getCellClass(value) {
      if (value === null || value === undefined || value === '') {
        return 'null-value'
      }
      return ''
    }
  }
}
</script>

<style scoped>
.table-container {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
  padding: 24px;
  margin: 0;
  max-width: 100%;
  overflow: hidden;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid #e5e7eb;
}

.header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.btn-add {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  box-shadow: 0 2px 4px rgba(102, 126, 234, 0.25);
}

.btn-add:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(102, 126, 234, 0.35);
}

.btn-add span {
  font-size: 18px;
  font-weight: bold;
}

.table-wrapper {
  overflow-x: auto;
  overflow-y: visible;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  max-width: 100%;
}

table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
  table-layout: auto;
}

thead {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: sticky;
  top: 0;
  z-index: 10;
}

thead th {
  color: white;
  font-weight: 600;
  text-align: left;
  padding: 14px 10px;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  white-space: nowrap;
  border-right: 1px solid rgba(255, 255, 255, 0.15);
}

thead th:last-child {
  border-right: none;
}

.expand-col { width: 36px; }
.expand-cell { text-align: center; }
.btn-expand {
  border: 1px solid #e5e7eb;
  background: white;
  border-radius: 4px;
  width: 24px;
  height: 24px;
  line-height: 22px;
  cursor: pointer;
}

.actions-header {
  text-align: center !important;
  min-width: 160px;
}

tbody tr {
  border-bottom: 1px solid #e5e7eb;
  transition: background-color 0.15s ease;
}

tbody tr:hover {
  background-color: #f9fafb;
}

tbody tr:last-child {
  border-bottom: none;
}

tbody td {
  padding: 12px 10px;
  color: #374151;
  white-space: nowrap;
  border-right: 1px solid #f3f4f6;
  font-size: 13px;
}

tbody td:last-child {
  border-right: none;
}

.expand-row td { background: #fbfbfd; }
.child-table-wrapper { padding: 10px 6px; }
.child-table { width: 100%; border-collapse: collapse; font-size: 12px; }
.child-table thead th { background: #eef2ff; color: #111827; padding: 8px; }
.child-table tbody td { padding: 8px; border-top: 1px solid #e5e7eb; }
.no-children { text-align: center; color: #6b7280; font-style: italic; }

.null-value {
  color: #9ca3af;
  font-style: italic;
  text-align: center;
}

.actions-cell {
  text-align: center;
  white-space: nowrap;
}

.read-only-label {
  color: #9ca3af;
  font-style: italic;
  font-size: 12px;
}

/* Sortable header styles */
th.sortable {
  cursor: pointer;
  user-select: none;
}

th.sortable .th-label {
  margin-right: 6px;
}

th.sortable:hover {
  background: rgba(255, 255, 255, 0.1);
}

th.sorted {
  color: #111827;
}

.sort-indicator {
  font-size: 11px;
  color: #6b7280;
}

.btn-edit,
.btn-delete {
  border: none;
  padding: 7px 14px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  margin: 0 3px;
}

.btn-edit {
  background: #3b82f6;
  color: white;
}

.btn-edit:hover {
  background: #2563eb;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(59, 130, 246, 0.3);
}

.btn-delete {
  background: #ef4444;
  color: white;
}

.btn-delete:hover {
  background: #dc2626;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(239, 68, 68, 0.3);
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #6b7280;
}

.spinner {
  border: 4px solid #f3f4f6;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  width: 48px;
  height: 48px;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading p {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.no-data {
  text-align: center;
  padding: 60px 20px;
  color: #6b7280;
}

.no-data p {
  font-size: 16px;
  margin: 0;
}

@media (max-width: 768px) {
  .table-container {
    padding: 16px;
  }

  .header h2 {
    font-size: 20px;
  }

  .btn-add {
    padding: 9px 16px;
    font-size: 13px;
  }
}
</style>

