# 🚗 AutoFuel — Gestão Veicular, Consumo & Manutenção

> Um aplicativo completo e inteligente para acompanhamento de abastecimentos, consumo médio real (km/L), alertas preventivos de manutenção e comparador econômico (Flex & Mercado).

Disponível em duas versões complementares:
1. **🌐 Versão Web / PWA (Progressive Web App)**: Roda direto no navegador de qualquer dispositivo (**Android, iPhone/iOS, iPad, Tablets e Desktop**), funcionando **100% offline** e podendo ser instalado na tela inicial.
2. **📱 Versão Android Nativa**: Construída em **Kotlin** com **Jetpack Compose**, banco de dados **Room** e **Material Design 3**.

---

## 🎯 O que é o AutoFuel e para que serve?

Manter um veículo no dia a dia envolve custos constantes que frequentemente passam despercebidos:
- **Dúvida no posto**: Etanol ou Gasolina? A regra padrão dos 70% nem sempre é verdadeira para o seu carro ou perfil de direção.
- **Revisões esquecidas**: Troca de óleo, pastilhas de freio, filtros e correias possuem prazos duplos (por quilometragem ou por tempo). Esquecer um prazo pode resultar em prejuízos mecânicos elevados.
- **Custo real por km rodado**: A maioria das pessoas só olha o preço do litro do combustível, sem saber quanto realmente gasta por quilômetro percorrido (R$/km) ou por mês.
- **Transição energética**: Vale a pena trocar por um carro híbrido ou elétrico? Quanto você economizaria por ano com base na sua rodagem mensal real?

O **AutoFuel** resolve todos esses problemas reunindo dados precisos, cálculos automáticos e uma experiência visual moderna e intuitiva.

---

## ✨ Principais Funcionalidades

### 1. 📊 Painel Geral & Odômetro Digital
- **Quilometragem com Atualização Rápida**: Atualize o odômetro do veículo com um toque.
- **Métricas Chave**:
  - **Consumo Médio (km/L)** ponderado por abastecimentos.
  - **Custo Médio por Quilômetro (R$/km)**.
  - **Gasto Total no Mês Atual (R$)** e acumulado.
  - **Total de Litros** abastecidos e último consumo medido.
- **Gráfico de Evolução de Consumo**: Curva interativa em alta definição que exibe a variação do consumo ao longo dos abastecimentos e a linha da média geral do veículo.
- **Alerta de Revisão em Destaque**: Avisa no topo do painel assim que qualquer manutenção estiver próxima ou vencida.

### 2. ⛽ Abastecimentos & Cálculo de Médias
- Registro rápido informando odômetro, litros, preço por litro e tipo de combustível (**Gasolina Comum**, **Aditivada**, **Etanol**, **Diesel** ou **GNV**).
- Opção **"Encheu o Tanque"** para cálculo exato de consumo entre abastecimentos consecutivos.
- Cálculo automático da distância percorrida desde o abastecimento anterior e o custo por quilômetro daquele tanque.
- Histórico completo organizado cronologicamente com postos e observações.

### 3. 🔧 Manutenção Preventiva Inteligente
- Monitoramento baseado na regra do que vencer primeiro: **Quilômetros** ou **Tempo (Meses)**.
- **Status Dinâmico em Tempo Real**:
  - 🟢 **Em Dia**: Manutenção dentro dos limites seguros.
  - 🟡 **Atenção (Próximo)**: Faltam menos de 1.000 km ou menos de 15 dias para vencer.
  - 🔴 **Vencido**: Quilometragem ou prazo ultrapassado, exigindo revisão imediata.
- **Barra de Progresso de Desgaste**: Visualização percentual do ciclo de vida de cada componente.
- **Registro de Serviços Realizados**: Grava o valor gasto, a oficina mecânica e observações, reiniciando o ciclo preventivo automaticamente e alimentando o histórico do veículo.
- Presets prontos para: *Óleo e filtro de motor*, *Filtro de ar e combustível*, *Pastilhas de freio*, *Alinhamento/balanceamento/rodízio*, *Fluido de freio DOT 4*, *Velas de ignição*, além de itens customizados.

### 4. ⚖️ Comparadores (2-em-1)

#### A. Calculadora Flex (Álcool x Gasolina)
- **Cálculo Baseado no Consumo Real do seu Carro**: Se você já abasteceu com gasolina e etanol, o AutoFuel calcula o ponto de equilíbrio personalizado do seu motor (por exemplo: 68.5% ou 73.2%), superando a regra genérica dos 70%.
- **Veredito Instantâneo**: Informa claramente se compensa abastecer com **ETANOL** ou **GASOLINA**.
- **Estimativa de Economia**: Calcula exatamente quantos reais você economiza por tanque cheio e a cada 1.000 km rodados.

#### B. Comparador de Mercado (Elétricos, Híbridos e Combustão)
- Simula o seu custo por quilômetro contra **11 veículos de referência do mercado brasileiro (dados Inmetro/PBEV)**:
  - **100% Elétricos**: *BYD Dolphin Mini*, *BYD Dolphin GS*, *GWM Ora 03*, *Volvo EX30*.
  - **Híbridos**: *Toyota Corolla Hybrid (HEV)*, *BYD Song Plus (PHEV)*, *GWM Haval H6 HEV*.
  - **Combustão Eficiente**: *Renault Kwid 1.0*, *Chevrolet Onix Plus 1.0*, *VW Polo 170 TSI*, *SUV Médio Turbo Flex Padrão*.
- Permite ajustar o preço da gasolina, o valor do kWh elétrico residencial e a sua média de km rodados por mês.
- Exibe a **economia anual estimada (R$/ano)** que você teria com cada modelo em relação ao seu carro atual!

### 5. 🚘 Gestão de Múltiplos Veículos & Backup
- Cadastro e troca rápida entre múltiplos carros ou motos.
- **Armazenamento 100% Local e Seguro**: Dados salvos localmente no dispositivo via `localStorage` / SQLite Room.
- **Exportação e Restauração em JSON**: Faça download do backup dos seus dados para transferir entre celulares ou guardar com segurança.
- **Modo Demonstração (Demo)**: Permite carregar dados de teste com um clique para explorar todas as telas.
- Alternador de tema **Escuro (Dark Mode)** e **Claro (Light Mode)**.

---

## 🌐 Acesso Online (PWA no GitHub Pages)

O AutoFuel PWA está pronto para ser acessado em:

👉 **[https://erickms11.github.io/AutoFuel/](https://erickms11.github.io/AutoFuel/)** *(após o deploy automático do GitHub Pages)*

### 📲 Como Instalar como Aplicativo no Celular:

- **No Android (Google Chrome / Edge)**:
  1. Abra o link no navegador.
  2. Toque no botão verde **"Instalar"** na barra superior (ou no menu `⋮` > *Adicionar à tela inicial* / *Instalar aplicativo*).
  3. O ícone do AutoFuel será instalado e abrirá em tela cheia como um app nativo!
- **No iPhone / iPad (Safari)**:
  1. Abra o link no Safari.
  2. Toque no botão de **Compartilhar** (ícone do quadrado com seta para cima na barra inferior).
  3. Selecione **"Adicionar à Tela de Início"** e confirme em **"Adicionar"**.

---

## 💻 Como Executar Localmente

### 1. Rodando a Versão Web (PWA):
```bash
# Entre na pasta web
cd web

# Inicie o servidor local (sem necessidade de dependências externas)
npm start
# ou: node server.js
```
Acesse no navegador: `http://localhost:3000`

### 2. Rodando o App Android Nativo:
1. Abra o projeto no **Android Studio** (Koala / Ladybug ou superior).
2. Aguarde a sincronização do Gradle.
3. Conecte um celular Android via USB (ou use um emulador) e clique no botão **Run (▶)**.

---

## 🛠️ Tecnologias Utilizadas

### Versão Web / PWA:
- **HTML5 & Vanilla JavaScript Moderno (ES Modules)**: Inicialização instantânea, zero dependências pesadas de terceiros.
- **Vanilla CSS3 Moderno**: Design system automotivo, glassmorphism, suporte completo a Safe Areas (iOS Notch) e responsividade para Mobile, Tablet e Desktop.
- **Service Worker (`sw.js`) & Web App Manifest**: Cache estático para funcionamento offline contínuo.
- **Canvas API**: Renderização de gráficos fluidos em alta definição.

### Versão Android Nativa:
- **Kotlin**: Linguagem oficial moderna para Android.
- **Jetpack Compose & Material 3**: UI declarativa, responsiva e com suporte a temas dinâmicos.
- **Room Database**: Persistência local SQLite segura com Coroutines e StateFlow.
- **Android Architecture Components**: ViewModel, StateFlow e ciclo de vida moderno.

---

## 📄 Licença
Este projeto é distribuído sob os termos da licença livre para fins educacionais e de uso pessoal.
