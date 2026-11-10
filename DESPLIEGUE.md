# Despliegue gratuito: Render + TiDB Cloud Starter

## Antes de empezar

- Este proyecto sigue funcionando localmente con NetBeans/Ant y `src/conf/db.properties`.
- El despliegue usa `pom.xml` + `Dockerfile`. El archivo `db.properties` **no se publica** ni se incluye en el contenedor; las credenciales van en las variables de entorno de Render.
- La API no tiene autenticación; no uses información médica real en un servicio público.

## 1. TiDB Cloud

1. Crea una instancia **TiDB Cloud Starter (MySQL-compatible)** gratuita en https://tidbcloud.com/.
2. En la instancia, pulsa **Connect**, elige conexión **Public** y copia host, puerto, usuario (incluido el prefijo, por ejemplo `abc123.root`) y contraseña. Conserva la contraseña en un lugar seguro; no la publiques en GitHub.
3. En **SQL Editor**, ejecuta `sql/tidb.sql`. Crea la base `gluco_api` y ambas tablas si no existen; no borra datos existentes.
4. Si configuraste reglas de firewall para el endpoint público, permite el tráfico saliente de Render según la configuración de TiDB Cloud. Se requiere conexión TLS.

## 2. GitHub

El proyecto ya está publicado en https://github.com/Wassito/GlucoAPI (rama `main`). Las credenciales locales de `src/conf/db.properties` están excluidas del repositorio.

## 3. Render

1. En https://dashboard.render.com/, conecta tu cuenta de GitHub y selecciona **New > Web Service** con tu repositorio.
2. Selecciona **Language: Docker** y **plan: Free**. El `Dockerfile` está en la raíz del proyecto.
3. En **Environment**, agrega (sin comillas):

   | Variable | Valor |
   | --- | --- |
   | `DB_URL` | `jdbc:mysql://gateway01.us-east-1.prod.aws.tidbcloud.com:4000/gluco_api?sslMode=VERIFY_IDENTITY&serverTimezone=UTC` |
   | `DB_USER` | `3kpxioAPQcduF5U.root` |
   | `DB_PASSWORD` | Contraseña generada en TiDB Cloud |

   Usa el **host y puerto que indique tu panel** de TiDB, no `localhost`. TiDB Starter exige TLS; `sslMode=VERIFY_IDENTITY` verifica el servidor. No copies el `useSSL=false` de la configuración local.

4. Si Render solicita un **Health Check Path**, indica `/api/glucosa/saludar` (no requiere base de datos). Inicia el despliegue.
5. El WAR se despliega como `ROOT.war`: la URL cambia a `https://NOMBRE.onrender.com/api/usuarios` (sin `/API_HealthBlood_512`).

## 4. Pruebas desde PowerShell

```powershell
$base = "https://NOMBRE.onrender.com/api"
curl.exe "$base/glucosa/saludar"
curl.exe "$base/usuarios"
curl.exe -X POST "$base/usuarios" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  --data-urlencode "nombre=Ana" `
  --data-urlencode "primerApellido=Garcia" `
  --data-urlencode "email=ana@example.com"
```

El POST responde `201` con `data.idUsuario`; usa ese id en el POST a `$base/medidas`. Si `/glucosa/saludar` responde y `/usuarios` devuelve `500`, revisa las variables `DB_*`, TLS, las tablas y los logs de Render.

**Límites gratuitos:** Render suspende servicios sin tráfico durante 15 minutos y el arranque puede tardar cerca de un minuto. TiDB Cloud Starter tiene una cuota mensual gratuita; verifica las condiciones de tu cuenta antes de habilitar facturación.
