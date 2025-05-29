<div align="center">
  <img src="https://raw.githubusercontent.com/rodrigoacs/flint/refs/heads/main/assets/logo/flint-logo.png" alt="drawing" width="200"/>
</div>

# Flint - File Linter

## 1. O que o sistema vai fazer?
O objetivo é criar uma ferramenta de linha de comando capaz de converter arquivos entre diferentes formatos. Por exemplo, .json -> .csv, .xml -> .json, .md -> .pdf.

## 2. Escopo geral do projeto
O programa será capaz de receber arquivos em formatos determinados. Irá transcrever para um formato padrão, e desse formato padrão será capaz de converter para qualquer outro formato.

### 2.1. Requisitos Funcionais (RF)
* RF01. A ferramenta deve permitir a conversão de arquivos entre formatos especificados (ex: .txt, .csv, .json, .xml, .md).
* RF02. O usuário deve ser capaz de especificar o arquivo de entrada e o formato de saída via argumentos de linha de comando.
* RF03. A ferramenta deve validar a existência e legibilidade do arquivo de entrada.
* RF04. A ferramenta deve validar se o formato de saída é suportado antes de realizar a conversão.
* RF05. A ferramenta deve exibir mensagens de erro claras em caso de falhas na leitura, conversão ou escrita do arquivo.
* RF06. A ferramenta deve fornecer uma opção de visualização da ajuda com instruções de uso (--help).

### 2.2. Requisitos Não Funcionais (RNF)

* RNF01. A ferramenta deve ser executável em ambientes Unix-like (Linux, macOS) e Windows.
* RNF02. O código-fonte da ferramenta deve ser modular e de fácil manutenção.
* RNF03. A ferramenta deve estar documentada, incluindo exemplos de uso para os principais formatos.
* RNF04. A interface de linha de comando deve seguir padrões de usabilidade, com parâmetros claros e coerentes.

## 3. Backlog 
* **História:** Definir e implementar a estrutura do "formato padrão" interno.
    * *Descrição:* Estabelecer a representação intermediária dos dados que permitirá a conversão entre diferentes formatos.
* **História:** Criar a arquitetura base para o fluxo de conversão (Entrada -> Padrão -> Saída).
    * *Descrição:* Desenvolver a estrutura básica do processo de conversão, conectando os módulos de leitura, transformação e escrita.
* **História (RF02):** Especificar o arquivo de entrada e o formato de saída através de argumentos na linha de comando para realizar uma conversão.
    * *Descrição:* Implementar a análise de argumentos (ex: `flint --input <arquivo> --output-format <formato>`).
* **História (RF06):** Visualizar uma mensagem de ajuda (`--help`) para entender como usar a ferramenta.
    * *Descrição:* Implementar a exibição de instruções de uso, opções e formatos suportados.
* **História (RF03):** A ferramenta deve verificar se o arquivo de entrada existe e pode ser lido antes de tentar a conversão.
    * *Descrição:* Implementar checagens de existência e permissões de leitura do arquivo de entrada.
* **História (RF04):** A ferramenta deve informar se o formato de saída escolhido é suportado.
    * *Descrição:* Verificar a viabilidade de conversão considerando as limitações do formato do arquivo de saída.
* **História (RF05):** Apresentar mensagens de erro claras se houver algum erro (arquivo não encontrado, formato inválido, erro de conversão).
    * *Descrição:* Implementar um sistema robusto de *error handling* e feedback para o usuário.
* **História (RF01):** Converter arquivos facilmente entre diferentes formatos (como .json, .csv, .xml, .md) usando uma única ferramenta de linha de comando.
    * *Descrição:* Implementar a funcionalidade central do Flint, permitindo que o usuário especifique um arquivo de entrada em um formato suportado e um formato de saída desejado. A ferramenta deve então realizar a conversão, processando a entrada, utilizando um formato interno para a transformação, e gerando o arquivo no formato de saída. Isso abrange a necessidade de ler e escrever JSON, CSV, XML, MD, TXT.
