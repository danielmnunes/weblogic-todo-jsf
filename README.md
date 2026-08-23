# weblogic-todo-jsf

Lista de tarefas com **Jakarta Server Faces 3.0**, **Java 21** e **JPA** no **WebLogic 15.1.1**, persistindo no **Oracle Database Free 23ai**.

A imagem local já usada no Podman é:

`container-registry.oracle.com/middleware/weblogic:15.1.1.0-generic-jdk21-ol9`

## Requisitos

- JDK 21
- Maven 3.9+
- Podman (Compose plugin) para o ambiente local

## Build e testes

```bash
mvn verify
```

O `verify` falha se a cobertura de **linhas** do JaCoCo ficar abaixo de **90%**. Relatório: `target/site/jacoco/index.html`.

Integração com Oracle via Testcontainers (precisa de Podman/Docker):

```bash
export TESTCONTAINERS_RYUK_DISABLED=true
export DOCKER_HOST="unix://${XDG_RUNTIME_DIR:-/run/user/$(id -u)}/podman/podman.sock"
mvn verify -Pit
```

## Subir app + banco no Podman

1. Empacote o WAR:

```bash
mvn -DskipTests package
```

2. Suba os containers (a imagem do WebLogic **não é baixada de novo** se já estiver local):

```bash
podman compose up --build
```

Na primeira subida o Oracle Free pode levar alguns minutos. Quando o WebLogic estiver pronto:

| Recurso | URL / dado |
| --- | --- |
| Aplicação | http://localhost:7001/todo |
| Console admin | http://localhost:7001/console |
| Usuário admin | `weblogic` / `Welcome1` |
| Oracle | `localhost:1521/FREEPDB1` |
| Usuário app | `todo` / `TodoPassword1` |

Para parar:

```bash
podman compose down
```

Os dados do Oracle ficam no volume `oracle-data`.

### Se a imagem do WebLogic ainda não existir

1. Entre em [Oracle Container Registry](https://container-registry.oracle.com), aceite a licença de `middleware/weblogic`.
2. Autentique o Podman:

```bash
podman login container-registry.oracle.com
podman pull container-registry.oracle.com/middleware/weblogic:15.1.1.0-generic-jdk21-ol9
```

## Stack

- Jakarta EE 9.1 (`jakarta.*`), Faces 3.0, CDI 3.0, JPA 3.1, Bean Validation 3.0
- APIs `provided` pelo WebLogic 15.1.1 (Mojarra + EclipseLink/TopLink)
- Datasource JNDI `jdbc/TodoDS`
- Modo desenvolvimento no container para autodeploy do WAR
