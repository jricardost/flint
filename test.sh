#!/bin/bash

# URL base da nossa API
API_URL="http://localhost:8080/api/convert"


echo "--- Criando arquivos de teste com dados equivalentes ---"

# Arquivo JSON
cat <<'EOF' >test_data.json
[
  {"name": "Alice", "city": "Sao Paulo"},
  {"name": "Bob", "city": "Rio de Janeiro"}
]
EOF
echo "✓ test_data.json criado."

# Arquivo CSV
cat <<'EOF' >test_data.csv
name,city
Alice,Sao Paulo
Bob,Rio de Janeiro
EOF
echo "✓ test_data.csv criado."

# Arquivo XML
cat <<'EOF' >test_data.xml
<root>
  <item>
    <name>Alice</name>
    <city>Sao Paulo</city>
  </item>
  <item>
    <name>Bob</name>
    <city>Rio de Janeiro</city>
  </item>
</root>
EOF
echo "✓ test_data.xml criado."
echo "--------------------------------------------------------"
echo

# --- Passo 2: Definir uma função para executar os testes ---

# Esta função torna o script mais limpo e legível
run_test() {
  local test_name="$1"
  local from_file="$2"
  local from_content_type="$3"
  local to_format="$4"
  local result_file="result_$(echo "$1" | tr ' -> ' '_').$to_format"

  echo "▶️  Testando: $test_name"

  # Usamos -s para modo silencioso e -o para salvar a saída em um arquivo
  curl -s -o "$result_file" \
    -F "file=@./$from_file;type=$from_content_type" \
    -F "to=$to_format" \
    "$API_URL"

  echo "    Resultado salvo em '$result_file':"
  # Adiciona um recuo para facilitar a leitura da saída
  sed 's/^/    /' "$result_file"
  echo "--------------------------------------------------------"
  echo
}

# --- Passo 3: Executar todas as 6 conversões ---

run_test "JSON para CSV" "test_data.json" "application/json" "csv"
run_test "JSON para XML" "test_data.json" "application/json" "xml"

run_test "CSV para JSON" "test_data.csv" "text/csv" "json"
run_test "CSV para XML" "test_data.csv" "text/csv" "xml"

run_test "XML para JSON" "test_data.xml" "application/xml" "json"
run_test "XML para CSV" "test_data.xml" "application/xml" "csv"

# --- Passo 4: Conclusão e Limpeza ---

echo "✅ Todos os testes foram concluídos."
echo "Você pode inspecionar os arquivos 'result_*.{json,csv,xml}' gerados."
echo "Para limpar os arquivos de teste e resultados, execute o comando abaixo:"
echo "rm result_*.{json,csv,xml} test_data.*"

# --- Fim do Script ---
