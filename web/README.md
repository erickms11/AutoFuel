# AutoFuel PWA - Versão Web Progressiva

Uma versão web progressiva (PWA) moderna, responsiva e de alta performance do **AutoFuel**, projetada para rodar em qualquer navegador e compatível com **Smartphones Android, iPhones (iOS), iPads/Tablets e Desktop**.

---

## 🚀 Como Iniciar Localmente

Para rodar o aplicativo no seu computador:

1. Abra o terminal na pasta `web`:
   ```bash
   cd web
   ```
2. Inicie o servidor local (sem necessidade de instalar pacotes pesados):
   ```bash
   npm start
   # ou: node server.js
   ```
3. Acesse no seu navegador:
   ```text
   http://localhost:3000
   ```

*(Se quiser testar no seu celular conectado na mesma rede Wi-Fi, basta acessar `http://<IP_DO_SEU_PC>:3000`)*.

---

## 📱 Como Instalar como Aplicativo (PWA)

O **AutoFuel PWA** funciona 100% offline e pode ser instalado direto na tela de início sem passar pela App Store ou Google Play:

### No Android (Google Chrome / Brave / Edge):
1. Acesse o endereço no Chrome.
2. O botão verde **"Instalar"** aparecerá na barra superior ou no banner da página.
3. Clique em **"Instalar"** (ou vá no menu de 3 pontinhos do navegador e escolha *"Adicionar à tela inicial"* / *"Instalar aplicativo"*).
4. O ícone do AutoFuel será adicionado ao menu de apps e abrirá em tela cheia, como um app nativo.

### No iPhone e iPad (Safari):
1. Abra o Safari e acesse a URL.
2. Toque no botão de **Compartilhar** (ícone do quadrado com a seta para cima, na barra inferior do Safari).
3. Role as opções para baixo e toque em **"Adicionar à Tela de Início"**.
4. Toque em **"Adicionar"** no canto superior direito.
5. Pronto! O aplicativo funcionará em tela cheia (standalone) com o ícone oficial automotivo do AutoFuel.

### No Computador (Desktop Chrome / Edge):
1. Clique no ícone de monitor/instalação que aparece na barra de endereço do navegador, ou no botão **"Instalar"** no topo da tela.

---

## ✨ Funcionalidades Incluídas

- **Painel Geral (Dashboard)**:
  - Odômetro digital com atualização rápida.
  - Alerta inteligente de manutenções pendentes/vencidas.
  - Ações rápidas para abastecer e gerenciar revisões.
  - Grid de métricas de consumo: Média geral (km/L), custo por km rodado (R$/km), gasto acumulado no mês e litros totais.
  - **Gráfico de tendência de consumo** interativo e renderizado em Canvas de alta resolução.
  - Lista dos últimos abastecimentos.

- **Abastecimentos**:
  - Registro detalhado com cálculo automático de valor total, preço por litro e odômetro.
  - Suporte a múltiplos combustíveis: Gasolina Comum, Aditivada, Etanol, Diesel e GNV.
  - Detecção de tanque cheio para cálculo de médias com precisão.
  - Histórico completo com possibilidade de exclusão.

- **Manutenções Preventivas**:
  - Acompanhamento por quilometragem e por tempo (meses).
  - Categorização: Motor, Filtros, Freios, Suspensão, Transmissão, Pneus, etc.
  - Status em tempo real: **Em Dia** (verde), **Atenção** (amarelo) e **Vencido** (vermelho).
  - Barra de progresso de desgaste.
  - Registro de serviços efetuados com valor, oficina e reinício automático do ciclo preventivo.
  - Histórico detalhado de manutenções realizadas.

- **Comparadores (2-em-1)**:
  - **Calculadora Flex**: Veredito instantâneo entre Etanol e Gasolina. Utiliza o consumo real medido do veículo do usuário (ou a paridade de 70% caso ainda não haja dados suficientes), calculando a economia em R$ por tanque e por 1.000 km.
  - **Comparador de Mercado**: 11 veículos de referência (100% Elétricos como BYD Dolphin e Volvo EX30, Híbridos como Corolla Hybrid e Song Plus, e Combustão eficiente como Onix Plus e Kwid). Simula o custo por km e a economia anual em R$/ano baseada na quilometragem mensal do usuário.

- **Gestão de Dados & Multi-veículo**:
  - Cadastro e alternância rápida entre múltiplos carros.
  - Armazenamento 100% offline no navegador (`localStorage`).
  - Exportação e importação de backups completos em formato `.JSON`.
  - Botão de restauração com dados de demonstração (Demo Data).
  - Alternador de tema **Escuro (Dark Mode)** e **Claro (Light Mode)**.
