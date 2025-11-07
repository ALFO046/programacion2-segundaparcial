Sistema de Gestión de Clientes
 Descripción general del sistema
Es una aplicación móvil desarrollada en Java para Android. Su propósito es facilitar el registro de visitas a clientes, integrando funcionalidades modernas como captura de imágenes, carga de archivos, persistencia local y sincronización con servidor.
Funcionalidades principales desarrolladas:
1. Formulario de datos del cliente.
   - Registro de CI, nombre completo, dirección y teléfono.
   - Captura de tres fotos del domicilio usando la cámara del dispositivo.
   - Envío de datos en formato JSON y fotos como archivos Multipart al servidor de prueba    mediante Retrofit.
2. Carga múltiple de archivos.
   - Selección de múltiples archivos desde el almacenamiento del dispositivo.
   - Compresión automática en un archivo ".zip".
   - Envío del archivo comprimido junto con el CI del cliente al servidor mediante Retrofit.
3. Registro local de errores (Auditoría).
   - Base de datos local implementada con Room.
   - Entidad "LogApp" con campos: id, fecha-Hora, descripcion-Error, clase-Origen.
   - Registro automático de errores capturados mediante bloques "try-catch".
4. Tarea programada con WorkManager
   - Ejecución automática cada 5 minutos.
   - Recuperación de registros de errores (logs_app), envío al servidor y eliminación tras confirmación de sincronización.
