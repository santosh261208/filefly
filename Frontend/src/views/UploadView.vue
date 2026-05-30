<template>
  <div class="page">
    <h1 class="title">FileFly</h1>

    <div v-if="!generatedLink" class="upload-box">
      <input type="file" @change="handleFileChange" />
      <button :disabled="!selectedFile || isUploading" @click="uploadFile" class="btn btn-primary">
        {{ isUploading ? 'Fliegt...' : 'Datei hochladen' }}
      </button>
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
const isUploading = ref(false)
const generatedLink = ref('')

const handleFileChange = (event) => {
  selectedFile.value = event.target.files[0]
}

const uploadFile = async () => {
  if (!selectedFile.value) return

  isUploading.value = true
  const formData = new FormData()
  formData.append('file', selectedFile.value)

  try {
    const response = await fetch('http://localhost:8080/api/files/upload', {
      method: 'POST',
      body: formData
    })

    if (!response.ok) throw new Error('Upload fehlgeschlagen')

    const data = await response.json()
    generatedLink.value = `http://localhost:5173/view/${data.shareId}`
  } catch (error) {
    alert(error.message)
  } finally {
    isUploading.value = false
  }
}

const copyToClipboard = () => {
  navigator.clipboard.writeText(generatedLink.value)
  alert('Link kopiert!')
}

const reset = () => {
  selectedFile.value = null
  generatedLink.value = ''
}
</script>

<style scoped>
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

.upload-box input[type="file"] {
  width: 100%;
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
</style>