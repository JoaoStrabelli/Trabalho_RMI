<h1 align="center">
    <img src="./src/main/resources/logo.png">
    WhatsUT 
</h1>
<h3 align="center">
💬  Aplicativo de sistema para comunicação interpessoal real-time utilizando Java RMI 
</h3>
<hr>

### Comandos

Lista de comandos utilizados no aplicativo
| Comando | Descrição |
| ----------- | ----------- |
| `/help` | Ajuda com os recursos |
| `/members` | Visualiza a listagem de membros |
| `/invites` | Visualiza a listagem de pedidos de entrar em um chat |
| `/accept {user_name}` | Aceitar um usuário específico no chat |
| `/ban {user_name}` | Banir um usuário específico no chat |
| `/exit` | Sair do chat |

> Caso o administrador do grupo saia, o aplicativo deve decidir quem será o novo administrador, ou se o grupo seja eliminado. Tal opção pode ser ajustada no momento da criação do chat em grupo.

### Diagrama de atividades

```mermaid
---
title: Diagrama de Atividades do Sistema WhatsUT
---
flowchart TD
    A[Usuário abre o aplicativo] --> B{Usuário possui conta?}
    B -->|Sim| C[Autenticação criptografada]
    C --> D{Login bem-sucedido?}
    D -->|Sim| E[Exibir lista de usuários]
    D -->|Não| F[Fim]

    B -->|Não| G[Realizar cadastro]
    G --> C

    E --> H[Exibir lista de grupos]
    H --> I{Usuário seleciona grupo?}
    I -->|Sim| J[Solicitar entrada no grupo]
    J --> K{Aprovado pelo criador do grupo?}
    K -->|Sim| L[Entrar no grupo]
    K -->|Não| H

    I -->|Não| M{Usuário seleciona chat privado?}
    M -->|Sim| N[Iniciar chat privado]
    N --> O[Enviar/receber mensagens e arquivos]

    M -->|Não| H

    L --> P[Iniciar chat em grupo]
    P --> Q[Enviar/receber mensagens]

    Q --> R{Usuário administrador do grupo?}
    R -->|Sim| S[Adicionar/remover usuários do grupo]
    R -->|Não| T[Participar do chat]

    S --> U{Administrador do grupo sai?}
    U -->|Sim| V[Designar novo administrador ou eliminar grupo]
    U -->|Não| H
```

### Diagrama de sequência

```mermaid
sequenceDiagram
    participant Usuário
    participant AplicativoWhatsUT
    participant ServidorWhatsUT
    participant CriadorDoGrupo
    participant OutroUsuário

    Usuário->>AplicativoWhatsUT: Abre aplicativo
    AplicativoWhatsUT->>Usuário: Tem conta? (Sim/Não)
    Usuário->>AplicativoWhatsUT: Sim

    AplicativoWhatsUT->>Usuário: Solicita credenciais
    Usuário->>AplicativoWhatsUT: Envia credenciais
    AplicativoWhatsUT->>ServidorWhatsUT: Autentica usuário
    ServidorWhatsUT->>AplicativoWhatsUT: Autenticação bem-sucedida

    AplicativoWhatsUT->>Usuário: Exibir lista de usuários
    AplicativoWhatsUT->>ServidorWhatsUT: Solicita lista de grupos
    ServidorWhatsUT->>AplicativoWhatsUT: Envia lista de grupos
    AplicativoWhatsUT->>Usuário: Exibir lista de grupos

    Usuário->>AplicativoWhatsUT: Seleciona grupo
    AplicativoWhatsUT->>ServidorWhatsUT: Solicita entrada no grupo
    ServidorWhatsUT->>CriadorDoGrupo: Notifica solicitação de entrada
    CriadorDoGrupo->>ServidorWhatsUT: Aprova solicitação
    ServidorWhatsUT->>AplicativoWhatsUT: Aprovação recebida
    AplicativoWhatsUT->>Usuário: Entrar no grupo

    Usuário->>AplicativoWhatsUT: Iniciar chat em grupo
    AplicativoWhatsUT->>ServidorWhatsUT: Enviar mensagem ao grupo
    ServidorWhatsUT->>CriadorDoGrupo: Notificar nova mensagem
    ServidorWhatsUT->>OutroUsuário: Notificar nova mensagem

    Usuário->>AplicativoWhatsUT: Seleciona chat privado
    AplicativoWhatsUT->>OutroUsuário: Notificar nova mensagem
    OutroUsuário->>AplicativoWhatsUT: Recebe notificação
    OutroUsuário->>AplicativoWhatsUT: Lê mensagem

    Usuario->>AplicativoWhatsUT: Realizar cadastro
    AplicativoWhatsUT->>ServidorWhatsUT: Solicita cadastro
    ServidorWhatsUT->>AplicativoWhatsUT: Cadastro realizado
    AplicativoWhatsUT->>Usuário: Exibir lista de usuários e grupos

    CriadorDoGrupo->>ServidorWhatsUT: Adicionar/remover usuários do grupo
    ServidorWhatsUT->>AplicativoWhatsUT: Atualização do grupo
    AplicativoWhatsUT->>Usuário: Exibir atualização do grupo

    CriadorDoGrupo->>ServidorWhatsUT: Sair do grupo
    ServidorWhatsUT->>AplicativoWhatsUT: Designar novo administrador ou eliminar grupo
    AplicativoWhatsUT->>Usuário: Exibir atualização do grupo

```

### Tecnologias utilizadas

- Java
- Java RMI
- Swing
