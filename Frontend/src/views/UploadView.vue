<template>
  <div class="page">
    <h1 class="title">FileFly</h1>

    <div v-if="!generatedLink" class="upload-box">
      <input type="file" @change="handleFileChange" />

      <input type="password" v-model="password" placeholder="Passwort (optional)" class="input-field" />
      <select v-model="expireHours" class="input-field">
        <option value="1">Ablauf in 1 Stunde</option>
        <option value="24">Ablauf in 24 Stunden</option>
        <option value="168">Ablauf in 7 Tagen</option>
      </select>

      <button :disabled="!selectedFile || isUploading" @click="uploadFile" class="btn btn-primary">
        {{ isUploading ? 'Fliegt...' : 'Datei hochladen' }}
      </button>

      <div v-if="isUploading" class="progress-wrapper">
        <div class="progress-bar" :style="{ width: uploadProgress + '%' }"></div>
        <span class="progress-label">{{ uploadProgress }}%</span>
      </div>
    </div>

    <div v-else class="result-box">
      <h3>Dein Download-Link ist bereit:</h3>
      <input type="text" readonly :value="generatedLink" class="link-input" />
      <div class="button-group">
        <button @click="copyToClipboard" class="btn btn-primary">
          Link kopieren
        </button>
        <button @click="reset" class="btn btn-ghost">
          Weitere Datei hochladen
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const selectedFile = ref(null)
const password = ref('')
const expireHours = ref('24') // Standard auf 24h
const isUploading = ref(false)
const uploadProgress = ref(0)
const generatedLink = ref('')

const handleFileChange = (event) => {
  selectedFile.value = event.target.files[0]
}

const uploadFile = () => {
  if (!selectedFile.value) return

  isUploading.value = true
  uploadProgress.value = 0

  const formData = new FormData()
  formData.append('file', selectedFile.value)

  // NEU: Security-Daten mitsenden
  if (password.value) {
    formData.append('password', password.value)
  }
  formData.append('expireHours', expireHours.value)

  // WIEDER DA: XMLHttpRequest für euren Fortschrittsbalken
  const xhr = new XMLHttpRequest()

  xhr.upload.addEventListener('progress', (event) => {
    if (event.lengthComputable) {
      uploadProgress.value = Math.round((event.loaded / event.total) * 100)
    }
  })

  xhr.addEventListener('load', () => {
    isUploading.value = false
    if (xhr.status >= 200 && xhr.status < 300) {
      const data = JSON.parse(xhr.responseText)
      generatedLink.value = `http://localhost:5173/view/${data.shareId}`
    } else {
      alert('Upload fehlgeschlagen')
    }
  })

  xhr.addEventListener('error', () => {
    isUploading.value = false
    alert('Upload fehlgeschlagen')
  })

  xhr.open('POST', 'http://localhost:8080/api/files/upload')
  xhr.send(formData)
}

const copyToClipboard = () => {
  navigator.clipboard.writeText(generatedLink.value)
  alert('Link kopiert!')
}

const reset = () => {
  selectedFile.value = null
  password.value = ''
  expireHours.value = '24'
  generatedLink.value = ''
  uploadProgress.value = 0
}
</script>

<style scoped>
/* Euer komplettes Original-CSS bleibt erhalten */
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 1.5rem;
}

.title {
  margin-bottom: 1.5rem;
  font-size: 2rem;
  font-weight: bold;
}

.upload-box {
  width: 100%;
  max-width: 480px;
  border: 2px dashed #ccc;
  border-radius: 8px;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.input-field {
  width: 100%;
  padding: 0.6rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 0.95rem;
}

.result-box {
  width: 100%;
  max-width: 480px;
  background: #e6f7ff;
  padding: 1.5rem;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.result-box h3 {
  font-weight: 600;
}

.link-input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 0.875rem;
  word-break: break-all;
}

.button-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

@media (max-width: 768px) {
  .upload-box input[type="file"] {
    width: 100%;
  }
}
@media (min-width: 480px) {
  .button-group {
    flex-direction: row;
    flex-wrap: wrap;
  }
}

.btn {
  padding: 0.6rem 1.2rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.95rem;
  border: none;
  transition: opacity 0.2s;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary {
  background: #1890ff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  opacity: 0.85;
}

.btn-ghost {
  background: none;
  border: 1px solid #ccc;
  color: gray;
}

.btn-ghost:hover {
  background: #f5f5f5;
}

.progress-wrapper {
  position: relative;
  width: 100%;
  height: 20px;
  background: #e0e0e0;
  border-radius: 10px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: #1890ff;
  border-radius: 10px;
  transition: width 0.2s ease;
}

.progress-label {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 600;
  color: #333;
}
</style>