# Prompt de integração do cadastro de livros, autores e editoras

Copie o prompt abaixo para o agente que vai atualizar o frontend:

---

Leia o `FRONTEND_HANDOFF.md` atualizado e inspecione o frontend antes de alterar qualquer arquivo. Implemente a atualização do cadastro e da consulta de livros, autores e editoras conforme este contrato:

1. No cadastro e na edição do livro, permita selecionar um autor/editora existente por autocomplete ou confirmar o texto digitado como um novo registro. Não exija que o usuário cadastre autor/editora em outra tela antes de criar o livro.
2. Para autocomplete/pesquisa, use `GET /api/livros/opcoes/autores?nome={termo}` e `GET /api/livros/opcoes/editoras?nome={termo}`. A resposta é uma lista de `{ "id": number, "nome": string }`. Sem `nome`, as rotas retornam todas as opções.
3. Ao selecionar uma opção existente, envie `autorId`/`editoraId`. Ao confirmar um nome novo, envie `autorNome`/`editoraNome`. O backend aceita nomes e cria o registro se ainda não existir; se já existir o mesmo nome (ignorando diferenças entre maiúsculas/minúsculas), reutiliza-o. Se ID e nome forem enviados ao mesmo tempo, o ID prevalece. Não envie IDs fictícios para nomes novos.
4. Mantenha os demais campos e validações exigidos pelo cadastro atual. Exemplo: `codigo`, `titulo`, `ano`, `edicao`, `isbn`, `numeroPaginas`, `sinopse`, `imagemUrl` (URL HTTP(S)), `codigoBarras`, `valorVenda`, `ativo`, `grupoPrecificacaoId`, `categoriaIds` com pelo menos uma categoria e `dimensao` (`altura`, `largura`, `profundidade`, `peso`). Estoque inicial continua sendo criado pelo backend; sua edição permanece separada.
5. No painel administrativo de autores/editoras, permita pesquisar pelos endpoints de opções acima. Ao selecionar um autor, carregue e exiba os livros associados com `GET /api/livros?autorId={id}`. Para editora, use `GET /api/livros?editoraId={id}`. Os dois parâmetros podem ser usados juntos e são combinados com AND. Sem esses parâmetros, `GET /api/livros` lista todos os livros.
6. Aproveite a resposta de `POST /api/livros`/`PUT /api/livros/{id}` para atualizar a interface; ela contém `autorId`, `autorNome`, `editoraId` e `editoraNome` resolvidos pelo backend.
7. Antes de editar, informe os arquivos/telas/serviços de API que encontrou, incompatibilidades com o contrato e um plano curto. Depois implemente a integração seguindo os padrões visuais e arquiteturais existentes, sem criar um segundo fluxo paralelo. Execute os testes disponíveis e reporte os arquivos alterados, verificações realizadas e pendências.

Não invente endpoints de CRUD separados para autor/editora, upload de capa, autenticação ou campos não descritos no contrato. Use a URL/API base já configurada no frontend.

---
