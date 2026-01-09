<template>
  <div id="app">

    <LoginForm
        v-if="!isLoggedIn"
        @login-success="onLoginSuccess"
    />

    <div v-else>
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

          <a @click="logout" style="cursor: pointer; background: rgba(0,0,0,0.2);">Logout</a>
        </div>
      </nav>

      <main>
        <DataTable
            :title="view.toUpperCase()"
            :columns="columns[view]"
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
  </div>
</template>

<script>
// ACHTUNG BEI DEN PFADEN:
// Wenn api.js und AuthService.js im selben Ordner wie App.vue liegen, nutze './'
// Wenn sie im Ordner 'services' liegen, nutze './services/'
import api from './services/api'
import AuthService from './services/AuthService'
import LoginForm from './components/LoginForm.vue' // <--- NEU: Login Komponente importieren

import DataTable from './components/DataTable.vue'
import EditModal from './components/EditModal.vue'
import AddModal from './components/AddModal.vue'

export default {
  // LoginForm hier registrieren
  components: { DataTable, EditModal, AddModal, LoginForm },

  computed: {
    totalPagesDisplay() {
      return this.totalPages || 1
    },
    effectiveSortBy() {
      return this.sortBy || this.defaultSortBy[this.view]
    }
  },
  data() {
    return {
      isLoggedIn: false, // <--- NEU: Status für Login
      view: 'analysis',
      data: [],
      loading: false,
      showEditModal: false,
      showAddModal: false,
      editingItem: null,
      currentPage: 0,
      pageSize: 20,
      totalElements: 0,
      totalPages: 1,
      sortBy: null,
      sortDir: 'desc',
      defaultSortBy: {
        analysis: 'a_id',
        sample: 's_stamp',
        box: 'b_id',
        boxpos: 'bpos_id',
        log: 'id'
      },
      expandedKeys: [],
      boxposByBox: {},
      boxposChildColumns: ['bpos_id', 's_id', 's_stamp', 'date_exported'],
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
      }
    }
  },
  watch: {
    view() {
      // Nur laden, wenn eingeloggt
      if(this.isLoggedIn) {
        this.currentPage = 0;
        this.sortBy = null;
        this.sortDir = 'desc';
        this.expandedKeys = [];
        this.boxposByBox = {};
        this.loadData();
      }
    }
  },
  mounted() {
    // KORRIGIERTER LOGIN CHECK:
    // Wir leiten NICHT weiter (kein window.location.href), sondern setzen nur den Status.
    // Das v-if im Template kümmert sich um den Rest.
    this.isLoggedIn = AuthService.isLoggedIn();

    if (this.isLoggedIn) {
      this.loadData();
    } else {
      console.log("Nicht eingeloggt. Zeige Login-Maske.");
    }
  },
  methods: {
    // --- NEUE METHODEN FÜR LOGIN ---
    onLoginSuccess() {
      this.isLoggedIn = true;
      this.loadData();
    },
    logout() {
      AuthService.logout();
      this.isLoggedIn = false;
      this.data = [];
    },

    // --- DEINE BESTEHENDEN METHODEN (UNVERÄNDERT) ---
    mapSortField(col) {
      if (!col) return 'id'
      const v = this.view
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
          's_id': 'id.sId',
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
        const da = Date.parse(va)
        const db = Date.parse(vb)
        const isDate = !isNaN(da) && !isNaN(db)
        if (isDate) {
          return (da - db) * dir
        }
        if (typeof va === 'number' && typeof vb === 'number') {
          return (va - vb) * dir
        }
        const na = Number(va)
        const nb = Number(vb)
        if (!isNaN(na) && !isNaN(nb)) {
          return (na - nb) * dir
        }
        return String(va).localeCompare(String(vb)) * dir
      })
      return sorted
    },
    async toggleExpand(row) {
      if (this.view !== 'box') return
      const key = row['b_id']
      const idx = this.expandedKeys.indexOf(key)
      if (idx >= 0) {
        this.expandedKeys.splice(idx, 1)
        return
      }
      this.expandedKeys.push(key)
      if (!this.boxposByBox[key]) {
        try {
          const items = await this.fetchAllBoxPos(key)
          this.$set ? this.$set(this.boxposByBox, key, items) : (this.boxposByBox[key] = items)
        } catch (e) {
          if (e.response && e.response.status === 403) return;

          console.error('Failed to load BoxPos for', key, e)
          this.$set ? this.$set(this.boxposByBox, key, []) : (this.boxposByBox[key] = [])
        }
      }
    },
    async fetchAllBoxPos(bId) {
      const first = await api.get(`boxpos`, {
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
                api.get(`boxpos`, { params: { bId, page: p, size: 500, sort: 'id.bposId,asc' } })
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
      // Sicherheits-Check
      if (!this.isLoggedIn) return;

      this.loading = true
      try {
        const res = await api.get(`${this.view}`, {
          params: {
            page: this.currentPage,
            size: this.pageSize,
            sort: `${this.mapSortField(this.effectiveSortBy)},${this.sortDir}`
          }
        })

        if (res.data && Array.isArray(res.data.content)) {
          this.data = res.data.content
          this.totalElements = typeof res.data.totalElements === 'number' ? res.data.totalElements : this.data.length
          this.totalPages = typeof res.data.totalPages === 'number' ? res.data.totalPages : Math.max(1, Math.ceil(this.totalElements / this.pageSize))

          if (this.currentPage >= this.totalPages && this.totalPages > 0) {
            this.currentPage = this.totalPages - 1
            return this.loadData()
          }
        } else if (Array.isArray(res.data)) {
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
        // Falls Token abgelaufen ist:
        if (err.response && err.response.status === 403) {
          this.logout();
          return;
        }

        console.error('Error loading data:', err)
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

      let createUrl = `${this.view}`

      try {
        const response = await api.post(createUrl, formData)
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
          updateUrl = `analysis/${updatedItem.a_id}`;
          break;
        case 'sample':
          updateUrl = `sample/${updatedItem.s_id}/${encodeURIComponent(updatedItem.s_stamp)}`;
          break;
        case 'box':
          updateUrl = `box/${updatedItem.b_id}`;
          break;
        case 'boxpos':
          updateUrl = `boxpos/${updatedItem.id.bId}/${updatedItem.id.bposId}`;
          break;
        default:
          alert('Unknown entity type');
          return;
      }

      try {
        await api[method](updateUrl, updatedItem);
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
          deleteUrl = `analysis/${item.a_id}`
          break;
        case 'sample':
          deleteUrl = `sample/${item.s_id}/${item.s_stamp}`
          break;
        case 'box':
          deleteUrl = `box/${item.b_id}`
          break;
        case 'boxpos':
          deleteUrl = `boxpos/${item.b_id}/${item.bpos_id}`
          break;
        default:
          alert('Unknown entity type')
          return
      }

      if (confirm(`Delete this ${this.view} record?`)) {
        try {
          await api.delete(deleteUrl)
          alert('Successfully deleted')
          this.loadData()
        } catch (err) {
          console.error('Delete failed:', err)
          alert(`Delete failed: ${err.response?.data?.message || err.message}`)
        }
      }
    }
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background: #f3f4f6;
  min-height: 100vh;
}

#app {
  min-height: 100vh;
  background: #f3f4f6;
}

nav {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
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
  color: white;
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
  background: white;
  border: 1px solid #e5e7eb;
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
  color: #374151;
}

</style>