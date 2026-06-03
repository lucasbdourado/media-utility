# Media Utility

Minimal Spring Boot and React scaffold for the Media Utility MVP.

## Build

```bash
./mvnw package
```

On Windows PowerShell:

```powershell
.\mvnw.cmd package
```

The Maven package build runs the frontend npm install/build workflow and copies `frontend/dist` into the Spring Boot static assets.

## Frontend Development

```bash
cd frontend
npm install
npm run dev
```

## Backend Development

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

## Melhorias

* Remover confirmação de zerar o contexto na execução da tarefa
* Instrucionar Verificação de Plan mode
* Melhorar e limitar atuação da IA reduzindo a quantidade de pensamento e uso de modelos mais leves
* Etapa de validação e revisão para planejamento
* Analisar ADRs e padronizar implementação de código para uso de DDD, Clean arch, de primeiro momento padronizar as nomenclaturas e padrão de código
* Salvar arquivos de planejamento de tasks mesmo com blocked
* Tasks separadas, mas criadas vazias
* Criar arquivo de state e controle de tasks para ver quais tasks estão bloqueadas
* Salvar alguns documentos em etapas sem confirmação
* Skill `create-tasks` precisa ter uma validação de tasks para garantir que todas as tasks foram criadas da forma correta, e preenchidas, de acordo com o project-planning
* Reduzir a quantidade e a necessidade de buscas e referencias, somente para a validação inicial.
* Validar a necessidade do uso da tech-spec
* Criação de rule, ou documento principal para busca de e resposta de padrão de código e arquitetura
* Não fugir do que foi proposto, se algo está mal esclarecido, pergunte, e as respostas viram documentos
* Criar ferramentas para testes
* Gerar ADRs se realmente for necessário
* Modelo de IA tem que se ater mais a instrução do que busca e raciocínio
* Não gere imagens
