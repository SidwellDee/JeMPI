# Set default variables 

export REACT_APP_NODE_ENV=${REACT_APP_NODE_ENV:-"development"}
export REACT_APP_MOCK_BACKEND=${REACT_APP_MOCK_BACKEND:-"false"}
export REACT_APP_JEMPI_BASE_API_HOST=${REACT_APP_JEMPI_BASE_API_HOST:-""}
export REACT_APP_JEMPI_BASE_API_PORT=${REACT_APP_JEMPI_BASE_API_PORT:-"50000"}
export REACT_APP_ENABLE_SSO=${REACT_APP_ENABLE_SSO:-"false"}
export REACT_APP_MAX_UPLOAD_CSV_SIZE_IN_MEGABYTES=${REACT_APP_MAX_UPLOAD_CSV_SIZE_IN_MEGABYTES:-"128"}
export KC_FRONTEND_URL=${KC_FRONTEND_URL:-""}
export KC_REALM_NAME=${KC_REALM_NAME:-""}
export KC_JEMPI_CLIENT_ID=${KC_JEMPI_CLIENT_ID:-""}
export REACT_APP_SHOW_BRAND_LOGO=${REACT_APP_SHOW_BRAND_LOGO:-"false"}
export REACT_APP_REFETCH_INTERVAL=${REACT_APP_REFETCH_INTERVAL:-"3000"}
export REACT_APP_CACHE_TIME=${REACT_APP_REACT_APP_CACHE_TIME:-"3000"}
export REACT_APP_STALE_TIME=${REACT_APP_REACT_APP_STALE_TIME:-"3000"}

cat /app/config-template.json | envsubst | tee /app/config.json 

serve -s /app
