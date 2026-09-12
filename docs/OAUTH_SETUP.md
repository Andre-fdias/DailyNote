# Configuração do Google Drive Backup

Como o aplicativo é para o seu uso pessoal e acessará uma pasta segura no Google Drive (AppData), é necessário que você autorize o seu aplicativo no **Google Cloud Console**. Caso contrário, o login falhará.

## Passos para Configuração

1. Acesse o [Google Cloud Console](https://console.cloud.google.com/).
2. Crie um novo projeto (ex: `DailyNotesBackup`).
3. Vá em **APIs e Serviços > Biblioteca** e ative a **Google Drive API**.
4. Vá em **Tela de consentimento OAuth**:
   - Escolha o tipo de usuário **Externo**.
   - Preencha os campos obrigatórios (nome do app, e-mail de suporte).
   - Adicione o escopo `https://www.googleapis.com/auth/drive.appdata`.
   - Adicione o seu próprio e-mail do Google (o que você usará no celular) como **Usuário de Teste**.
5. Vá em **Credenciais > Criar Credenciais > ID do cliente OAuth**.
   - Escolha **Aplicativo Android**.
   - Nome do pacote: `com.andrefdias.dailynote`.
   - **Impressão digital do certificado SHA-1**: Você deve inserir a chave SHA-1 que assina o seu APK. 

## Como obter a chave SHA-1 (Debug)
No seu Android Studio, você pode rodar a tarefa Gradle `signingReport` no painel lateral direito, ou usar o terminal:
```bash
./gradlew signingReport
```
Copie o SHA-1 listado para a variante `debug` e cole no Google Cloud Console.

> **Importante**: Se você gerar o APK final assinado (Release), precisará adicionar também o SHA-1 dessa chave de produção no Google Cloud Console!
