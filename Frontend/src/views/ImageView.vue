<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const backendImageUrl = computed(() => {
  return `http://localhost:8080/api/files/download/${route.params.id}`
})

// Download via Blob, damit der Browser nicht einfach einen neuen Tab öffnet
const forceDownload = async () => {
  try {
    const response = await fetch(backendImageUrl.value)
    if (!response.ok) throw new Error("Error occurred.")

    const blob = await response.blob()

    const localUrl = window.URL.createObjectURL(blob)
    const tempLink = document.createElement('a')
    tempLink.href = localUrl
    tempLink.setAttribute('download', `filefly_${route.params.id}`)
    document.body.appendChild(tempLink)
    tempLink.click()
    document.body.removeChild(tempLink)
    window.URL.revokeObjectURL(localUrl)

  } catch (error) {
    console.error("Error while downloading:", error)
    alert("The Picture could not be downloaded.")
  }
}
</script>

<template>
  <div class="page">
    <h2 class="subtitle">Bild: {{ route.params.id }}</h2>

    <img :src="backendImageUrl" class="preview-image" />

    <div class="button-group">
      <button @click="$router.push('/')" class="btn btn-secondary">
        Neues Bild hochladen
      </button>
      <button @click="forceDownload" class="btn btn-primary">
        Herunterladen
      </button>
    </div>
  </div>
</template>

<style scoped>
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