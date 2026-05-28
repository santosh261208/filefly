<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// Holt die ID aus der URL und baut den Backend-Link für die Anzeige
const backendImageUrl = computed(() => {
  return `http://localhost:8080/api/files/download/${route.params.id}`
})

//Download it this way, because otherwise it just opens it in a new tab
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
  <div style="position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 20px;">
    <h2>Bild: {{route.params.id}}</h2>

    <img :src="backendImageUrl" style="max-width: 100%; max-height: 60vh; object-fit: contain; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);" />

    <div style="display: flex; gap: 15px; margin-top: 20px;">
      <button @click="$router.push('/')" class="buttonStyle">
        Neues Bild hochladen
      </button>

      <button @click="forceDownload" class="buttonStyle">
        Herunterladen
      </button>
    </div>
  </div>
</template>


<style scoped>
.buttonStyle {
  padding: 10px 20px;
}
</style>