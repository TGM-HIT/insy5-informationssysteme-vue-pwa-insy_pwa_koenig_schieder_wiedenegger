<template>
  <div id="app" :class="theme">
    <nav>
      <div class="nav-brand">
        <h1>Lab Data Management</h1>
      </div>
      <div class="nav-links">
        <a @click="view='analysis'" :class="{active: view=='analysis'}" data-cy="nav-analysis">Analysis</a>
        <a @click="view='sample'" :class="{active: view=='sample'}" data-cy="nav-sample">Sample</a>
        <a @click="view='box'" :class="{active: view=='box'}" data-cy="nav-box">Box</a>
        <a @click="view='boxpos'" :class="{active: view=='boxpos'}" data-cy="nav-boxpos">BoxPos</a>
        <a @click="view='log'" :class="{active: view=='log'}" data-cy="nav-log">Log</a>
      </div>
      <button @click="toggleTheme" class="theme-switch-btn">
        {{ theme === 'light' ? '🌙' : '☀️' }}
      </button>
    </nav>

    <main>

      <div class="table-controls">
        <ColumnSelector
            :all-columns="columns[view]"
            :visibility="columnVisibility[view]"
            @toggle="toggleColumn"
            @select-all="selectAllColumns"
            @reset="resetColumns"
        />
      </div>

      <DataTable
          :title="view.toUpperCase()"
          :columns="visibleColumns"
          :data="data"
          :loading="loading"
          :sort-by="effectiveSortBy"
          :sort-dir="sortDir"
          :expandable="view==='box'"
          :expand-key="'b_id'"
          :expanded-keys="expandedKeys"
          :child-map="boxposByBox"
          :child-columns="boxposChildColumns"
          @toggle-expand="toggleExpand"
          @sort-change="onSortChange"
          @add="addItem"
          @edit="editItem"
          @delete="deleteItem"
      />

      <div class="pagination" v-if="totalPages > 1 || totalElements > pageSize">
        <button class="btn" @click="prevPage" :disabled="currentPage === 0" data-cy="btn-prev">Previous</button>
        <span class="page-info" data-cy="page-info">Page {{ currentPage + 1 }} of {{ totalPagesDisplay }} ({{ totalElements }} total)</span>
        <button class="btn" @click="nextPage" :disabled="currentPage >= totalPages - 1" data-cy="btn-next">Next</button>
      </div>
    </main>

    <EditModal
        :show="showEditModal"
        :item="editingItem"
        :entity-type="view"
        :fields="columns[view]"
        @close="closeEditModal"
        @save="saveItem"
    />

    <AddModal
        :show="showAddModal"
        :entity-type="view"
        :fields="columns[view]"
        @close="closeAddModal"
        @add="addNewItem"
    />
  </div>
</template>


<script>
import axios from 'axios'
import DataTable from './components/DataTable.vue'
import EditModal from './components/EditModal.vue'
import AddModal from './components/AddModal.vue'
import ColumnSelector from './components/ColumnSelector.vue'

const API = 'http://localhost:8081/api'

export default {
  components: { DataTable, EditModal, AddModal, ColumnSelector },
  computed: {
    totalPagesDisplay() {
      return this.totalPages || 1
    },
    effectiveSortBy() {
      return this.sortBy || this.defaultSortBy[this.view]
    },
    visibleColumns() {
      const view = this.view
      const allCols = this.columns[view]
      const visibility = this.columnVisibility[view]

      // Filter nur sichtbare Spalten
      return allCols.filter(col => {
        // Default: alle Spalten sichtbar wenn nicht definiert
        return visibility[col] !== false
      })
    }
  },
  data() {
    return {
      theme: 'light',
      view: 'analysis',
      data: [],
      loading: false,
      showEditModal: false,
      showAddModal: false,
      editingItem: null,
      // pagination state
      currentPage: 0,
      pageSize: 20,
      totalElements: 0,
      totalPages: 1,
      // sorting state
      sortBy: null,
      sortDir: 'desc',
      defaultSortBy: {
        analysis: 'a_id',
        sample: 's_stamp',
        box: 'b_id',
        boxpos: 'bpos_id',
        log: 'id'
      },
      // expansion state for Box view
      expandedKeys: [],
      boxposByBox: {},
      boxposChildColumns: ['bpos_id', 's_id', 's_stamp', 'date_exported'],
      // columns config
      columns: {
        analysis: [
          'a_id', 's_id', 's_stamp', 'pol', 'nat', 'kal', 'an', 'glu', 'dry',
          'date_in', 'date_out', 'weight_mea', 'weight_nrm', 'weight_cur', 'weight_dif',
          'density', 'a_flags', 'lane', 'comment', 'date_exported'
        ],
        sample: [
          's_id', 's_stamp', 'name', 'weight_net', 'weight_bru', 'weight_tar',
          'quantity', 'distance', 'date_crumbled', 's_flags', 'lane', 'comment', 'date_exported'
        ],
        box: [
          'b_id', 'name', 'num_max', 'type', 'comment', 'date_exported'
        ],
        boxpos: [
          'bpos_id', 'b_id', 's_id', 's_stamp', 'date_exported'
        ],
        log: ['id', 'dateCreated', 'level', 'info', 'sId', 'sStamp', 'aId', 'dateExported']
      },

      // Column visibility state
      columnVisibility: {
        analysis: {},
        sample: {},
        box: {},
        boxpos: {},
        log: {}
      },
    }
  },
  watch: {
    view() {
      this.currentPage = 0;
      this.sortBy = null;
      this.sortDir = 'desc';
      // reset expansion state when changing views
      this.expandedKeys = [];
      this.boxposByBox = {};
      this.loadData();
    }
  },
  mounted() {
    this.loadTheme();
    this.loadColumnVisibility();
    this.loadData();
  },
  methods: {
    toggleTheme() {
      this.theme = this.theme === 'light' ? 'dark' : 'light';
      localStorage.setItem('theme', this.theme);
    },
    loadTheme() {
      const savedTheme = localStorage.getItem('theme');
      if (savedTheme) {
        this.theme = savedTheme;
      }
    },
    mapSortField(col) {
      if (!col) return 'id'
      const v = this.view
      // special mappings per view
      const special = {
        analysis: {
          'a_id': 'id',
          's_id': 'sId',
          's_stamp': 'sStamp',
          'date_in': 'dateIn',
          'date_out': 'dateOut',
          'weight_mea': 'weightMea',
          'weight_nrm': 'weightNrm',
          'weight_cur': 'weightCur',
          'weight_dif': 'weightDif',
          'a_flags': 'aFlags',
          'date_exported': 'dateExported'
        },
        sample: {
          's_id': 'id.sId', // not pageable but keep mapping
          's_stamp': 'id.sStamp',
          'weight_net': 'weightNet',
          'weight_bru': 'weightBru',
          'weight_tar': 'weightTar',
          'date_crumbled': 'dateCrumbled',
          's_flags': 'sFlags',
          'date_exported': 'dateExported'
        },
        box: {
          'b_id': 'id',
          'num_max': 'numMax',
          'date_exported': 'dateExported'
        },
        boxpos: {
          'bpos_id': 'id.bposId',
          'b_id': 'id.bId',
          's_id': 'sampleId',
          's_stamp': 'sampleStamp',
          'date_exported': 'dateExported'
        },
        log: {}
      }
      const map = special[v] || {}
      if (map[col]) return map[col]
      // generic snake_case -> camelCase
      if (col.includes('_')) {
        return col.replace(/_([a-z])/g, (_, c) => c.toUpperCase())
      }
      return col
    },
    onSortChange(col) {
      if (this.sortBy === col) {
        this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc'
      } else {
        this.sortBy = col
        this.sortDir = 'asc'
      }
      this.currentPage = 0
      this.loadData()
    },
    clientSort(array) {
      const col = this.effectiveSortBy
      const dir = this.sortDir === 'asc' ? 1 : -1
      const sorted = [...array].sort((a, b) => {
        const va = a[col]
        const vb = b[col]
        if (va == null && vb == null) return 0
        if (va == null) return 1
        if (vb == null) return -1
        // try date
        const da = Date.parse(va)
        const db = Date.parse(vb)
        const isDate = !isNaN(da) && !isNaN(db)
        if (isDate) {
          return (da - db) * dir
        }
        // numbers
        if (typeof va === 'number' && typeof vb === 'number') {
          return (va - vb) * dir
        }
        // numeric strings
        const na = Number(va)
        const nb = Number(vb)
        if (!isNaN(na) && !isNaN(nb)) {
          return (na - nb) * dir
        }
        // strings
        return String(va).localeCompare(String(vb)) * dir
      })
      return sorted
    },
    async toggleExpand(row) {
      if (this.view !== 'box') return
      const key = row['b_id']
      const idx = this.expandedKeys.indexOf(key)
      if (idx >= 0) {
        // collapse
        this.expandedKeys.splice(idx, 1)
        return
      }
      // expand
      this.expandedKeys.push(key)
      if (!this.boxposByBox[key]) {
        try {
          const items = await this.fetchAllBoxPos(key)
          this.$set ? this.$set(this.boxposByBox, key, items) : (this.boxposByBox[key] = items)
        } catch (e) {
          console.error('Failed to load BoxPos for', key, e)
          this.$set ? this.$set(this.boxposByBox, key, []) : (this.boxposByBox[key] = [])
          alert(`Error loading positions for box ${key}: ${e.response?.data?.message || e.message}`)
        }
      }
    },
    async fetchAllBoxPos(bId) {
      // load first page to determine total pages
      const first = await axios.get(`${API}/boxpos`, {
        params: { bId, page: 0, size: 500, sort: 'id.bposId,asc' }
      })
      let content = []
      if (first.data && Array.isArray(first.data.content)) {
        content = first.data.content
        const totalPages = typeof first.data.totalPages === 'number' ? first.data.totalPages : 1
        if (totalPages > 1) {
          const promises = []
          for (let p = 1; p < totalPages; p++) {
            promises.push(
                axios.get(`${API}/boxpos`, { params: { bId, page: p, size: 500, sort: 'id.bposId,asc' } })
            )
          }
          const results = await Promise.all(promises)
          for (const r of results) {
            if (r.data && Array.isArray(r.data.content)) content = content.concat(r.data.content)
          }
        }
      } else if (Array.isArray(first.data)) {
        content = first.data.filter(it => it.b_id === bId)
      }
      return content
    },
    async loadData() {
      this.loading = true
      try {
        const res = await axios.get(`${API}/${this.view}`, {
          params: {
            page: this.currentPage,
            size: this.pageSize,
            sort: `${this.mapSortField(this.effectiveSortBy)},${this.sortDir}`
          }
        })

        if (res.data && Array.isArray(res.data.content)) {
          // Pageable response
          this.data = res.data.content
          this.totalElements = typeof res.data.totalElements === 'number' ? res.data.totalElements : this.data.length
          this.totalPages = typeof res.data.totalPages === 'number' ? res.data.totalPages : Math.max(1, Math.ceil(this.totalElements / this.pageSize))

          // If requested page is out of bounds (e.g., after deletion), go to last page
          if (this.currentPage >= this.totalPages && this.totalPages > 0) {
            this.currentPage = this.totalPages - 1
            return this.loadData()
          }
        } else if (Array.isArray(res.data)) {
          // Non-pageable response: client-side sort + paginate
          const sorted = this.clientSort(res.data)
          this.totalElements = sorted.length
          this.totalPages = Math.max(1, Math.ceil(this.totalElements / this.pageSize))
          const start = this.currentPage * this.pageSize
          const end = start + this.pageSize
          this.data = sorted.slice(start, end)

          if (this.currentPage >= this.totalPages && this.totalPages > 0) {
            this.currentPage = this.totalPages - 1
            return this.loadData()
          }
        } else {
          this.data = []
          this.totalElements = 0
          this.totalPages = 1
        }
      } catch (err) {
        console.error('Error loading data:', err)
        alert(`Error loading ${this.view}: ${err.response?.data?.message || err.message}`)
        this.data = []
        this.totalElements = 0
        this.totalPages = 1
      } finally {
        this.loading = false
      }
    },

    nextPage() {
      if (this.currentPage < this.totalPages - 1) {
        this.currentPage++
        this.loadData()
      }
    },

    prevPage() {
      if (this.currentPage > 0) {
        this.currentPage--
        this.loadData()
      }
    },

    addItem() {
      if (this.view === 'log') {
        alert('Adding log entries is not allowed')
        return
      }
      this.showAddModal = true
    },

    closeAddModal() {
      this.showAddModal = false
    },

    async addNewItem(formData) {
      if (this.view === 'log') return

      let createUrl = `${API}/${this.view}`

      try {
        const response = await axios.post(createUrl, formData)
        console.log('Created:', response.data)
        alert('Successfully created')
        this.closeAddModal()
        this.loadData()
      } catch (err) {
        console.error('Create failed:', err)
        const errorMsg = err.response?.data?.message || err.response?.data || err.message
        alert(`Create failed: ${errorMsg}`)
      }
    },

    editItem(item) {
      if (this.view === 'log') {
        alert('Editing log entries is not allowed')
        return
      }
      this.editingItem = { ...item }
      this.showEditModal = true
    },

    closeEditModal() {
      this.showEditModal = false
      this.editingItem = null
    },

    async saveItem(updatedItem) {
      if (this.view === 'log') return;

      let updateUrl = '';
      let method = 'put';

      switch(this.view) {
        case 'analysis':
          updateUrl = `${API}/analysis/${updatedItem.a_id}`;
          break;
        case 'sample':
          updateUrl = `${API}/sample/${updatedItem.s_id}/${encodeURIComponent(updatedItem.s_stamp)}`;
          break;
        case 'box':
          updateUrl = `${API}/box/${updatedItem.b_id}`;
          break;
        case 'boxpos':
          updateUrl = `${API}/boxpos/${updatedItem.id.bId}/${updatedItem.id.bposId}`;
          break;
        default:
          alert('Unknown entity type');
          return;
      }

      try {
        await axios[method](updateUrl, updatedItem);
        alert('Successfully updated');
        this.closeEditModal();
        this.loadData();
      } catch (err) {
        console.error('Update failed:', err);
        alert(`Update failed: ${err.response?.data?.message || err.message}`);
      }
    },

    async deleteItem(item) {
      if (this.view === 'log') {
        alert('Deleting log entries is not allowed')
        return
      }

      let deleteUrl = ''

      switch(this.view) {
        case 'analysis':
          deleteUrl = `${API}/analysis/${item.a_id}`
          break
        case 'sample':
          deleteUrl = `${API}/sample/${item.s_id}/${item.s_stamp}`
          break
        case 'box':
          deleteUrl = `${API}/box/${item.b_id}`
          break
        case 'boxpos':
          deleteUrl = `${API}/boxpos/${item.b_id}/${item.bpos_id}`
          break
        default:
          alert('Unknown entity type')
          return
      }

      if (confirm(`Delete this ${this.view} record?`)) {
        try {
          await axios.delete(deleteUrl)
          alert('Successfully deleted')
          this.loadData()
        } catch (err) {
          console.error('Delete failed:', err)
          alert(`Delete failed: ${err.response?.data?.message || err.message}`)
        }
      }
    },

    loadColumnVisibility() {
      const saved = localStorage.getItem('columnVisibility')
      if (saved) {
        this.columnVisibility = JSON.parse(saved)
      } else {
        // Initialisiere mit allen Spalten sichtbar
        Object.keys(this.columns).forEach(view => {
          this.columnVisibility[view] = {}
          this.columns[view].forEach(col => {
            this.columnVisibility[view][col] = true
          })
        })
      }
    },

    saveColumnVisibility() {
      localStorage.setItem('columnVisibility', JSON.stringify(this.columnVisibility))
    },

    toggleColumn(col) {
      this.columnVisibility[this.view][col] = !this.columnVisibility[this.view][col]
      this.saveColumnVisibility()
    },

    selectAllColumns() {
      const view = this.view
      this.columns[view].forEach(col => {
        this.columnVisibility[view][col] = true
      })
      this.saveColumnVisibility()
    },

    resetColumns() {
      const view = this.view
      this.columns[view].forEach(col => {
        this.columnVisibility[view][col] = true
      })
      this.saveColumnVisibility()
    }
  }
}
</script>

<style>
:root {
  --bg-color: #f3f4f6;
  --text-color: #111827;
  --nav-bg: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --nav-text: white;
  --btn-bg: white;
  --btn-border: #e5e7eb;
  --btn-text: #374151;
}

.dark {
  --bg-color: #111827;
  --text-color: #f3f4f6;
  --nav-bg: linear-gradient(135deg, #374151 0%, #111827 100%);
  --nav-text: #f3f4f6;
  --btn-bg: #374151;
  --btn-border: #4b5563;
  --btn-text: #f3f4f6;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background: var(--bg-color);
  color: var(--text-color);
  min-height: 100vh;
}

#app {
  min-height: 100vh;
  background: var(--bg-color);
}

nav {
  background: var(--nav-bg);
  color: var(--nav-text);
  padding: 18px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.nav-brand h1 {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.3px;
}

.nav-links {
  display: flex;
  gap: 6px;
}

.nav-links a {
  color: var(--nav-text);
  cursor: pointer;
  padding: 9px 18px;
  border-radius: 7px;
  transition: all 0.2s ease;
  font-weight: 500;
  font-size: 14px;
  user-select: none;
}

.nav-links a:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-1px);
}

.nav-links a.active {
  background: rgba(255, 255, 255, 0.25);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.theme-switch-btn {
  background: none;
  border: none;
  color: var(--nav-text);
  font-size: 24px;
  cursor: pointer;
}

main {
  max-width: calc(100vw - 64px);
  margin: 24px auto;
  padding: 0 32px;
}

@media (max-width: 768px) {
  nav {
    flex-direction: column;
    gap: 12px;
    padding: 14px 16px;
  }

  .nav-links {
    flex-wrap: wrap;
    justify-content: center;
  }

  main {
    max-width: 100%;
    padding: 0 16px;
    margin: 16px auto;
  }
}
.pagination {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
}

.pagination .btn {
  background: var(--btn-bg);
  border: 1px solid var(--btn-border);
  color: var(--btn-text);
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s ease, box-shadow 0.2s ease;
}

.pagination .btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination .btn:hover:not(:disabled) {
  background: #f9fafb;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}

.pagination .page-info {
  font-size: 14px;
  color: var(--text-color);
}

.table-controls {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
  gap: 12px;
}


</style>
