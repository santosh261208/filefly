<template>
  <div class="page">
    <h2 v-if="loading" class="subtitle">Prüfe Link...</h2>

    <template v-else-if="notFound">
      <div class="error-box">
        <span class="error-icon">&#128247;</span>
        <h2>Link ungültig oder abgelaufen</h2>
        <p>Die Datei existiert nicht oder die Frist ist verstrichen.</p>
        <button @click="$router.push('/')" class="btn btn-primary">
          Zurück zur Startseite
        </button>
      </div>
    </template>

    <template v-else-if="requiresPassword && !imageLoaded">
      <div class="error-box" style="max-width: 400px;">
        <span class="error-icon">&#128274;</span>
        <h2>Datei ist geschützt</h2>
        <p>Bitte gib das Passwort ein, um die Datei zu sehen.</p>

        <input type="password" v-model="inputPassword" placeholder="Passwort eingeben" style="width: 100%; padding: 0.6rem; border: 1px solid #ccc; border-radius: 4px; margin-bottom: 10px;" />
        <p v-if="authError" style="color: red; font-size: 0.9rem; margin-bottom: 10px;">Falsches Passwort!</p>

        <button @click="fetchFile" class="btn btn-primary" style="width: 100%;">
          Datei entsperren
        </button>
      </div>
    </template>

    <template v-else-if="imageLoaded">
      <h2 class="subtitle">Bild: {{ route.params.id }}</h2>
      <img :src="blobUrl" class="preview-image" @error="notFound = true" />

      <div class="button-group">
        <button @click="$router.push('/')" class="btn btn-secondary">
          Neues Bild hochladen
        </button>
        <button @click="triggerDownload" class="btn btn-primary">
          Herunterladen
        </button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const loading = ref(true)
const notFound = ref(false)
const requiresPassword = ref(false)
const inputPassword = ref('')
const authError = ref(false)
const imageLoaded = ref(false)
const blobUrl = ref(null)

onMounted(async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/files/info/${route.params.id}`)
    if (!res.ok) {
      notFound.value = true
      loading.value = false
      return
    }
    const data = await res.json()

    if (data.requiresPassword) {
      requiresPassword.value = true
      loading.value = false
    } else {
      await fetchFile()
    }
  } catch (e) {
    notFound.value = true
    loading.value = false
  }
})

const fetchFile = async () => {
  authError.value = false
  loading.value = true
  try {
    const res = await fetch(`http://localhost:8080/api/files/download/${route.params.id}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ password: inputPassword.value })
    })

    if (res.status === 401) {
      authError.value = true
      loading.value = false
      return
    }
    if (!res.ok) throw new Error()

    const blob = await res.blob()
    blobUrl.value = window.URL.createObjectURL(blob)
    imageLoaded.value = true
  } catch (error) {
    notFound.value = true
  } finally {
    loading.value = false
  }
}

const triggerDownload = () => {
  const tempLink = document.createElement('a')
  tempLink.href = blobUrl.value
  tempLink.setAttribute('download', `filefly_${route.params.id}`)
  document.body.appendChild(tempLink)
  tempLink.click()
  document.body.removeChild(tempLink)
}
</script>

<style scoped>
/* Euer Original-CSS! */
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 1.5rem;
  gap: 1.25rem;
}

.subtitle {
  font-size: 1.1rem;
  font-weight: 600;
  word-break: break-all;
  text-align: center;
}

.preview-image {
  width: 100%;
  max-width: 640px;
  max-height: 60vh;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.error-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  text-align: center;
  max-width: 360px;
}

.error-icon {
  font-size: 3rem;
}

.error-box h2 {
  font-size: 1.4rem;
  font-weight: 700;
}

.error-box p {
  color: #666;
  font-size: 0.95rem;
}

.button-group {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  width: 100%;
  max-width: 320px;
}

@media (min-width: 480px) {
  .button-group {
    flex-direction: row;
    max-width: none;
    width: auto;
  }
}

.btn {
  padding: 0.6rem 1.4rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.95rem;
  border: none;
  transition: opacity 0.2s;
  text-align: center;
}

.btn-primary {
  background: #1890ff;
  color: white;
}

.btn-primary:hover {
  opacity: 0.85;
}

.btn-secondary {
  background: #f0f0f0;
  color: #333;
  border: 1px solid #d9d9d9;
}

.btn-secondary:hover {
  background: #e0e0e0;
}
</style>