<template>
  <div style="position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 20px;">
    <h1>FileFly</h1>

    <div v-if="!generatedLink" style="border: 2px dashed #ccc; padding: 20px; text-align: center;">
      <input type="file" @change="handleFileChange" />
      <button :disabled="!selectedFile || isUploading" @click="uploadFile" style="margin-top: 10px;">
        {{ isUploading ? 'Fliegt...' : 'Datei hochladen' }}
      </button>
    </div>

    <div v-else style="background: #e6f7ff; padding: 20px; margin-top: 20px; border-radius: 5px;">
      <h3>Dein Download-Link ist bereit:</h3>
      <input type="text" readonly :value="generatedLink" style="width: 100%; padding: 5px;" />

      <button @click="copyToClipboard" style="margin-top: 10px;">
        Link kopieren
      </button>
      <button @click="reset" style="margin-top: 10px; margin-left: 10px; background: none; border: none; color: gray; cursor: pointer;">
        Weitere Datei hochladen
      </button>
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