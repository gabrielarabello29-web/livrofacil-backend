-- LivroFácil: carga idempotente de 50 obras publicadas com ISBN-13 e capas Open Library.
-- Fontes de edição e capa: https://openlibrary.org/books/{edition-key}
-- Capas mapeadas pelo ID de capa da edição: https://covers.openlibrary.org/b/id/{cover-id}-L.jpg?default=false
-- ISBNs, datas/anos, editoras, páginas e IDs de capa foram consultados nos registros Open Library.
-- Nota: liv_edicao = 1 é o valor inicial exigido pelo modelo; o Open Library não fornece
-- número de edição bibliográfico consistente para todas as edições selecionadas.
-- Preço, estoque inicial e dimensões são valores de teste, não dados bibliográficos.
-- Não altera nem apaga registros permanentes. A tabela temporária persiste após COMMIT
-- para permitir as consultas de validação abaixo; ela é descartada ao fechar a sessão.

BEGIN;

DROP TABLE IF EXISTS pg_temp.seed_livros_50;
CREATE TEMPORARY TABLE seed_livros_50 (
    titulo text NOT NULL,
    ano integer NOT NULL,
    isbn text NOT NULL,
    paginas integer NOT NULL,
    autor text NOT NULL,
    editora text NOT NULL,
    cover_id bigint NOT NULL,
    edition_key text NOT NULL,
    sinopse text NOT NULL,
    categorias text[] NOT NULL,
    grupo text NOT NULL,
    valor numeric(10,2) NOT NULL CHECK (valor > 0),
    disponivel integer NOT NULL CHECK (disponivel >= 0),
    altura numeric(10,2) NOT NULL,
    largura numeric(10,2) NOT NULL,
    profundidade numeric(10,2) NOT NULL,
    peso numeric(10,2) NOT NULL
) ON COMMIT PRESERVE ROWS;

INSERT INTO seed_livros_50
(titulo, ano, isbn, paginas, autor, editora, cover_id, edition_key, sinopse, categorias, grupo, valor, disponivel, altura, largura, profundidade, peso)
VALUES
('1984', 2020, '9788194215813', 262, 'George Orwell', 'Books Worldwide', 15196195, 'OL61427489M', 'Em um Estado totalitário, Winston Smith tenta preservar a memória e a autonomia enquanto é vigiado pelo Partido e pelo Grande Irmão.', ARRAY['Ficção científica','Literatura estrangeira','Clássicos'], 'Intermediário 40%', 39.90, 18, 20.00, 13.00, 2.00, 0.32),
('To Kill a Mockingbird', 1997, '9780099419785', 309, 'Harper Lee', 'Arrow Books Ltd', 15269230, 'OL62697490M', 'Pelos olhos da jovem Scout, uma comunidade do sul dos Estados Unidos enfrenta preconceito e injustiça durante um julgamento marcante.', ARRAY['Ficção','Literatura estrangeira','Clássicos'], 'Básico 30%', 44.90, 24, 21.00, 14.00, 2.20, 0.38),
('The Great Gatsby', 2020, '9783730600009', 431, 'F. Scott Fitzgerald', 'Anaconda', 14369845, 'OL48590937M', 'Na Nova York da década de 1920, o narrador Nick Carraway observa a riqueza, as festas e os desejos que cercam Jay Gatsby.', ARRAY['Ficção','Romance','Literatura estrangeira','Clássicos'], 'Intermediário 40%', 34.90, 15, 20.00, 13.00, 2.50, 0.42),
('Pride and Prejudice', 2017, '9787511024831', 456, 'Jane Austen', 'Dolphin Books', 14543311, 'OL43294729M', 'Elizabeth Bennet e Mr. Darcy precisam rever suas primeiras impressões em meio às convenções sociais e aos romances da Inglaterra rural.', ARRAY['Romance','Literatura estrangeira','Clássicos'], 'Básico 30%', 29.90, 29, 21.00, 14.00, 2.70, 0.46),
('Jane Eyre', 1993, '9780582096707', 168, 'Charlotte Brontë', 'Addison-Wesley Longman, Limited', 15164202, 'OL34367780M', 'A órfã Jane constrói sua independência e enfrenta segredos e dilemas afetivos ao trabalhar como preceptora em Thornfield Hall.', ARRAY['Romance','Literatura estrangeira','Clássicos'], 'Intermediário 40%', 32.90, 12, 20.00, 13.00, 1.50, 0.27),
('Wuthering Heights', 2021, '9798542843742', 478, 'Emily Brontë', 'RAS Publishing', 14840570, 'OL57650591M', 'A paixão turbulenta de Catherine Earnshaw e Heathcliff atravessa gerações nas charnecas inglesas.', ARRAY['Romance','Literatura estrangeira','Clássicos'], 'Premium 50%', 39.90, 20, 21.00, 14.00, 2.80, 0.48),
('Frankenstein', 2013, '9788415089452', 384, 'Mary Shelley', 'Plutón Ediciones', 8078328, 'OL26385062M', 'Victor Frankenstein dá vida a uma criatura e precisa confrontar as consequências de sua ambição e da rejeição que se segue.', ARRAY['Ficção científica','Terror','Literatura estrangeira','Clássicos'], 'Intermediário 40%', 35.90, 17, 21.00, 14.00, 2.40, 0.40),
('The Hobbit', 2001, '9783125777705', 364, 'J.R.R. Tolkien', 'Klett', 996169, 'OL9018092M', 'Bilbo Bolseiro deixa sua vida tranquila para acompanhar anões e o mago Gandalf em uma jornada até a Montanha Solitária.', ARRAY['Fantasia','Infantil','Literatura estrangeira','Clássicos'], 'Premium 50%', 49.90, 14, 21.00, 14.00, 2.30, 0.39),
('The Fellowship of the Ring', 2022, '9780008537722', 448, 'J.R.R. Tolkien', 'HarperCollins Publishers', 15244480, 'OL62417661M', 'Frodo recebe a missão de levar o Um Anel para longe do Condado e parte com companheiros rumo a uma jornada perigosa.', ARRAY['Fantasia','Literatura estrangeira'], 'Premium 50%', 59.90, 21, 23.00, 15.00, 3.00, 0.55),
('Harry Potter and the Philosopher''s Stone', 2014, '9781408855652', 332, 'J.K. Rowling', 'Bloomsbury', 15159585, 'OL59004869M', 'No aniversário de onze anos, Harry descobre sua origem bruxa e começa uma nova vida na Escola de Magia e Bruxaria de Hogwarts.', ARRAY['Fantasia','Infantil','Literatura estrangeira'], 'Premium 50%', 54.90, 28, 20.00, 13.00, 2.20, 0.36),
('The Lion, the Witch and the Wardrobe', 2009, '9780007323128', 171, 'C.S. Lewis', 'HarperCollins Children''s Books', 10123765, 'OL28187491M', 'Quatro irmãos atravessam um guarda-roupa e chegam a Nárnia, uma terra sob o inverno da Feiticeira Branca.', ARRAY['Fantasia','Infantil','Literatura estrangeira','Clássicos'], 'Básico 30%', 34.90, 11, 20.00, 13.00, 1.70, 0.28),
('Dune', 2005, '9780441013593', 544, 'Frank Herbert', 'Ace Trade', 14565843, 'OL7524304M', 'Paul Atreides chega a Arrakis, planeta desértico e centro da produção da especiaria que sustenta disputas políticas e religiosas.', ARRAY['Ficção científica','Fantasia','Literatura estrangeira'], 'Premium 50%', 64.90, 19, 23.00, 15.00, 3.40, 0.62),
('Fahrenheit 451', 2008, '9780006546061', 192, 'Ray Bradbury', 'HarperVoyager', 15265555, 'OL62675371M', 'Em uma sociedade que proíbe livros, o bombeiro Guy Montag começa a questionar a destruição do conhecimento.', ARRAY['Ficção científica','Literatura estrangeira','Clássicos'], 'Intermediário 40%', 32.90, 16, 20.00, 13.00, 1.80, 0.30),
('Brave New World', 2020, '9786074536782', 88, 'Aldous Huxley', 'Selector', 15126937, 'OL36633894M', 'Uma sociedade organizada por condicionamento e consumo é confrontada pela chegada de alguém criado fora de seu sistema.', ARRAY['Ficção científica','Literatura estrangeira','Clássicos'], 'Básico 30%', 27.90, 13, 19.00, 12.00, 1.20, 0.22),
('The Handmaid''s Tale', 2018, '9781784874872', 320, 'Margaret Atwood', 'Vintage Classics', 15265551, 'OL62675369M', 'Offred vive sob um regime que controla os corpos e a reprodução das mulheres e narra sua busca por memória e liberdade.', ARRAY['Ficção científica','Literatura estrangeira'], 'Premium 50%', 47.90, 22, 21.00, 14.00, 2.20, 0.38),
('The Book Thief', 2016, '9780735284296', 552, 'Markus Zusak', 'Random House', 13645243, 'OL38586194M', 'Na Alemanha durante a Segunda Guerra, Liesel encontra refúgio nos livros e nas relações que constrói em meio à violência.', ARRAY['Ficção','História','Literatura estrangeira'], 'Premium 50%', 49.90, 10, 23.00, 15.00, 3.30, 0.58),
('The Kite Runner', 2013, '9781408845820', 352, 'Khaled Hosseini', 'Bloomsbury Publishing', 15162788, 'OL61140725M', 'Amir relembra a amizade de infância com Hassan e retorna ao Afeganistão para enfrentar escolhas do passado.', ARRAY['Ficção','Literatura estrangeira'], 'Intermediário 40%', 39.90, 18, 21.00, 14.00, 2.20, 0.37),
('A Thousand Splendid Suns', 2007, '9781594489518', 384, 'Khaled Hosseini', 'Brand: Riverhead Books', 15160111, 'OL39477397M', 'Mariam e Laila, de gerações diferentes, formam um vínculo de resistência e cuidado em meio às mudanças políticas do Afeganistão.', ARRAY['Ficção','História','Literatura estrangeira'], 'Intermediário 40%', 42.90, 25, 21.00, 14.00, 2.40, 0.40),
('The Little Prince', 2018, '9781946963147', 94, 'Antoine de Saint-Exupéry', 'Albatross Publishers', 13890892, 'OL46958658M', 'Um piloto encontra no deserto um pequeno viajante que conta sobre seu planeta, suas viagens e os encontros que teve.', ARRAY['Infantil','Fantasia','Literatura estrangeira','Clássicos'], 'Básico 30%', 24.90, 38, 18.00, 12.00, 1.00, 0.19),
('The Alchemist', 2006, '9780061122415', 197, 'Paulo Coelho', 'HarperOne', 45762, 'OL9242938M', 'O pastor Santiago parte em busca de um tesouro e encontra pessoas e experiências que transformam sua jornada.', ARRAY['Ficção','Desenvolvimento pessoal','Literatura estrangeira'], 'Intermediário 40%', 34.90, 20, 20.00, 13.00, 1.80, 0.30),
('Sapiens', 2021, '9780063212220', 256, 'Yuval Noah Harari', 'Harper', 11481587, 'OL32849073M', 'Uma síntese interpretativa da história humana, das sociedades antigas às instituições e tecnologias contemporâneas.', ARRAY['História','Ciência'], 'Premium 50%', 69.90, 32, 23.00, 15.00, 2.10, 0.40),
('Educated', 2018, '9781987146127', 354, 'Tara Westover', 'Random House Audio', 15216613, 'OL45153788M', 'Em suas memórias, Tara Westover relata uma infância isolada e o percurso de formação que a levou à universidade.', ARRAY['Biografia','Desenvolvimento pessoal','Literatura estrangeira'], 'Premium 50%', 59.90, 9, 21.00, 14.00, 2.20, 0.38),
('Atomic Habits', 2018, '9780735211292', 320, 'James Clear', 'Avery, an Imprint of Penguin Random House LLC', 12886417, 'OL32336498M', 'James Clear apresenta estratégias para construir hábitos, reduzir comportamentos indesejados e sustentar mudanças graduais.', ARRAY['Desenvolvimento pessoal','Educação'], 'Intermediário 40%', 54.90, 27, 23.00, 15.00, 2.00, 0.39),
('How to Win Friends and Influence People', 2022, '9781785044229', 320, 'Dale Carnegie', 'Vermilion', 15262530, 'OL62656710M', 'Um guia de comunicação interpessoal que aborda escuta, cooperação e relações profissionais.', ARRAY['Desenvolvimento pessoal','Educação'], 'Básico 30%', 39.90, 23, 21.00, 14.00, 2.00, 0.35),
('Man''s Search for Meaning', 2006, '9781663607980', 192, 'Viktor Frankl', 'Perfection Learning Corporation', 13243866, 'OL38084899M', 'Viktor Frankl combina o relato de sua experiência em campos de concentração com reflexões sobre sentido e psicologia.', ARRAY['Biografia','Desenvolvimento pessoal','História'], 'Intermediário 40%', 37.90, 12, 20.00, 13.00, 1.70, 0.29),
('The Art of War', 1996, '9780804830805', 109, 'Sun Tzu', 'Charles E Tuttle Co', 578637, 'OL7930826M', 'Tratado clássico atribuído a Sun Tzu, composto por reflexões sobre estratégia, planejamento e conflito.', ARRAY['História','Clássicos','Educação'], 'Básico 30%', 24.90, 30, 18.00, 12.00, 1.20, 0.22),
('The Immortal Life of Henrietta Lacks', 2010, '9781410427922', 619, 'Rebecca Skloot', 'Thorndike Press', 12571825, 'OL24523817M', 'A jornalista Rebecca Skloot investiga a história de Henrietta Lacks e o impacto científico e ético de suas células.', ARRAY['Biografia','Ciência','História'], 'Premium 50%', 74.90, 8, 23.00, 15.00, 3.80, 0.68),
('A Brief History of Time', 1995, '9780553175219', 240, 'Stephen Hawking', 'Bantam Books Ltd', 368368, 'OL7819689M', 'Stephen Hawking apresenta conceitos de cosmologia, espaço, tempo e origem do universo para leitores não especializados.', ARRAY['Ciência','Educação'], 'Intermediário 40%', 44.90, 16, 21.00, 14.00, 1.80, 0.32),
('Cosmos', 1980, '9780394502946', 365, 'Carl Sagan', 'Random House', 10601318, 'OL24740450M', 'Carl Sagan percorre a história da ciência e do universo, relacionando descobertas científicas à experiência humana.', ARRAY['Ciência','Educação'], 'Premium 50%', 69.90, 14, 25.00, 20.00, 2.50, 0.70),
('The Selfish Gene', 2016, '9780198788607', 496, 'Richard Dawkins', 'Oxford University Press', 15179535, 'OL28000467M', 'Richard Dawkins expõe uma perspectiva centrada nos genes para discutir evolução e comportamento.', ARRAY['Ciência','Educação'], 'Premium 50%', 64.90, 10, 22.00, 14.00, 2.80, 0.48),
('On the Origin of Species', 1986, '9780451627766', 242, 'Charles Darwin', 'Signet', 295937, 'OL7589508M', 'A obra de Charles Darwin apresenta evidências e argumentos sobre seleção natural e a diversidade das espécies.', ARRAY['Ciência','História','Clássicos'], 'Intermediário 40%', 39.90, 13, 20.00, 13.00, 1.80, 0.31),
('The Structure of Scientific Revolutions', 2012, '9780226458113', 217, 'Thomas S. Kuhn', 'The University of Chicago Press', 12131388, 'OL25067548M', 'Thomas Kuhn analisa como mudanças de paradigma transformam as práticas e os conceitos das comunidades científicas.', ARRAY['Ciência','Educação'], 'Premium 50%', 59.90, 7, 23.00, 15.00, 1.80, 0.34),
('A People''s History of the United States', 2003, '9780965607414', 754, 'Howard Zinn', 'harper collins publishers', 15190793, 'OL61423016M', 'Uma interpretação da história dos Estados Unidos que destaca movimentos sociais e grupos frequentemente marginalizados.', ARRAY['História','Literatura estrangeira'], 'Premium 50%', 79.90, 11, 23.00, 15.00, 4.20, 0.75),
('The Guns of August', 1976, '9780553026696', 575, 'Barbara Tuchman', 'Bantam Books', 9899148, 'OL7816509M', 'Barbara Tuchman examina as decisões políticas e militares que marcaram o início da Primeira Guerra Mundial.', ARRAY['História','Literatura estrangeira'], 'Intermediário 40%', 54.90, 10, 23.00, 15.00, 3.50, 0.60),
('The Republic', 1992, '9781857150988', 408, 'Plato', 'Everyman''s Library', 15264516, 'OL62674300M', 'No diálogo de Platão, Sócrates e seus interlocutores investigam justiça, educação e a organização da cidade ideal.', ARRAY['Filosofia','Clássicos','Literatura estrangeira'], 'Intermediário 40%', 44.90, 16, 21.00, 14.00, 2.60, 0.45),
('The Odyssey', 2004, '9781587261411', 267, 'Homer', 'Ann Arbor Media Group', 10792515, 'OL32130157M', 'O poema épico acompanha as provações de Odisseu no longo retorno para casa após a Guerra de Troia.', ARRAY['Fantasia','Clássicos','Literatura estrangeira'], 'Intermediário 40%', 37.90, 18, 21.00, 14.00, 2.00, 0.34),
('Dom Casmurro', 1998, '9780195103090', 286, 'Machado de Assis', 'Oxford University Press, USA', 123225, 'OL7387820M', 'Bentinho narra sua relação com Capitu e suas dúvidas sobre o passado em uma obra central do realismo brasileiro.', ARRAY['Literatura brasileira','Romance','Clássicos'], 'Premium 50%', 39.90, 26, 20.00, 13.00, 2.10, 0.36),
('The Posthumous Memoirs of Bras Cubas', 2012, '9788506004234', 272, 'Machado de Assis', 'Melhoramentos', 15160864, 'OL61095027M', 'Brás Cubas, narrador já morto, revisita sua vida com ironia e comenta a sociedade brasileira do século XIX.', ARRAY['Literatura brasileira','Ficção','Clássicos'], 'Premium 50%', 42.90, 12, 20.00, 13.00, 2.00, 0.35),
('The Hour of the Star', 2011, '9780811219495', 81, 'Clarice Lispector', 'New Directions', 9260944, 'OL24896411M', 'O narrador acompanha Macabéa, uma jovem nordestina que vive no Rio de Janeiro, e questiona como contar sua história.', ARRAY['Literatura brasileira','Ficção','Clássicos'], 'Intermediário 40%', 29.90, 21, 19.00, 12.00, 1.00, 0.19),
('The Invisible Man', 2016, '9781541245259', 192, 'H.G. Wells', 'CreateSpace Independent Publishing Platform', 14866889, 'OL55801070M', 'Um cientista descobre como se tornar invisível, mas enfrenta isolamento e consequências imprevistas de sua experiência.', ARRAY['Ficção científica','Literatura estrangeira','Clássicos'], 'Básico 30%', 29.90, 14, 20.00, 13.00, 1.60, 0.28),
('The Road', 2006, '9780307277923', 287, 'Cormac McCarthy', 'Vintage International', 15235434, 'OL62234436M', 'Em um mundo devastado, um pai e seu filho atravessam a estrada tentando sobreviver e manter a esperança.', ARRAY['Ficção','Suspense','Literatura estrangeira'], 'Premium 50%', 44.90, 9, 21.00, 14.00, 2.00, 0.35),
('The Shining', 2007, '9781848940994', 512, 'Stephen King', 'Hodder & Stoughton', 14817043, 'OL53243897M', 'Jack Torrance aceita trabalhar em um hotel isolado durante o inverno; forças inquietantes ameaçam sua família.', ARRAY['Terror','Suspense','Literatura estrangeira'], 'Premium 50%', 59.90, 17, 23.00, 15.00, 3.20, 0.56),
('And Then There Were None', 2004, '9780671754013', 264, 'Agatha Christie', 'St. Martin''s Griffin', 15244938, 'OL49808404M', 'Dez convidados isolados em uma ilha percebem que estão sendo acusados e eliminados um a um.', ARRAY['Suspense','Literatura estrangeira','Clássicos'], 'Intermediário 40%', 34.90, 22, 20.00, 13.00, 2.00, 0.33),
('The Girl with the Dragon Tattoo', 2009, '9781906694661', 542, 'Stieg Larsson', 'Vintage Crime / Black Lizard', 10871094, 'OL32237058M', 'O jornalista Mikael Blomkvist e a investigadora Lisbeth Salander apuram o desaparecimento de uma jovem de uma família influente.', ARRAY['Suspense','Literatura estrangeira'], 'Premium 50%', 54.90, 14, 23.00, 15.00, 3.30, 0.58),
('The Name of the Rose', 1984, '9780330284141', 502, 'Umberto Eco', 'Picador', 14652004, 'OL52972117M', 'Em uma abadia medieval, o frade William de Baskerville investiga mortes misteriosas enquanto seu noviço observa.', ARRAY['Suspense','História','Literatura estrangeira'], 'Premium 50%', 49.90, 12, 23.00, 15.00, 3.10, 0.54),
('The Very Hungry Caterpillar', 2011, '9780448455976', 7, 'Eric Carle', 'Penguin Young Readers Group', 14596314, 'OL28747538M', 'Uma lagarta come diferentes alimentos ao longo da semana e se transforma em borboleta, em um livro ilustrado para crianças.', ARRAY['Infantil','Educação'], 'Básico 30%', 29.90, 40, 18.00, 13.00, 1.00, 0.22),
('Charlotte''s Web', 1972, '9781663611499', 194, 'E.B. White', 'Turtleback', 13117913, 'OL44969426M', 'A amizade entre o porquinho Wilbur e a aranha Charlotte muda o destino de Wilbur na fazenda.', ARRAY['Infantil','Ficção','Literatura estrangeira','Clássicos'], 'Básico 30%', 32.90, 19, 20.00, 13.00, 1.70, 0.29),
('The Picture of Dorian Gray', 2024, '9781400344161', 262, 'Oscar Wilde', 'Harper Muse, an imprint of HarperCollins Focus LLC', 15265884, 'OL62694670M', 'Dorian Gray permanece jovem enquanto seu retrato registra os efeitos de suas escolhas e de sua busca por prazer.', ARRAY['Ficção','Terror','Literatura estrangeira','Clássicos'], 'Intermediário 40%', 39.90, 16, 21.00, 14.00, 2.00, 0.35),
('The Secret Garden', 2007, '9781406844955', 168, 'Frances Hodgson Burnett', 'Echo Library', 13623921, 'OL39844443M', 'Mary Lennox descobre um jardim abandonado e, com novos amigos, acompanha sua recuperação e a transformação de suas vidas.', ARRAY['Infantil','Ficção','Literatura estrangeira','Clássicos'], 'Básico 30%', 27.90, 24, 20.00, 13.00, 1.50, 0.26),
('Becoming', 2021, '9780593303740', 432, 'Michelle Obama', 'Delacorte Books for Young Readers', 10618645, 'OL31936187M', 'Michelle Obama relata momentos de sua infância, trajetória profissional e experiência como primeira-dama dos Estados Unidos.', ARRAY['Biografia','Literatura estrangeira'], 'Premium 50%', 64.90, 8, 23.00, 15.00, 2.70, 0.49);

DO $$
BEGIN
    IF (SELECT count(*) FROM seed_livros_50) <> 50 THEN
        RAISE EXCEPTION 'A carga precisa conter exatamente 50 linhas; encontradas: %',
            (SELECT count(*) FROM seed_livros_50);
    END IF;
END $$;

-- Autores e editoras não têm UNIQUE no schema atual; evita duplicidade por nome sem supor IDs.
INSERT INTO autor (aut_nome)
SELECT DISTINCT s.autor
FROM seed_livros_50 s
WHERE NOT EXISTS (
    SELECT 1 FROM autor a WHERE lower(trim(a.aut_nome)) = lower(trim(s.autor))
);

INSERT INTO editora (edi_nome)
SELECT DISTINCT s.editora
FROM seed_livros_50 s
WHERE NOT EXISTS (
    SELECT 1 FROM editora e WHERE lower(trim(e.edi_nome)) = lower(trim(s.editora))
);

INSERT INTO categoria (cat_nome)
SELECT DISTINCT c.nome
FROM seed_livros_50 s
CROSS JOIN LATERAL unnest(s.categorias) AS c(nome)
WHERE NOT EXISTS (
    SELECT 1 FROM categoria existente
    WHERE lower(trim(existente.cat_nome)) = lower(trim(c.nome))
)
ON CONFLICT (cat_nome) DO NOTHING;

INSERT INTO grupo_precificacao (grp_pre_nome, grp_pre_percentual_margem)
VALUES ('Básico 30%', 30.00), ('Intermediário 40%', 40.00), ('Premium 50%', 50.00)
ON CONFLICT (grp_pre_nome) DO NOTHING;

-- Os preços são gravados diretamente; não há cálculo de liv_valor_venda pela margem.
INSERT INTO livro (
    liv_codigo, liv_titulo, liv_ano, liv_edicao, liv_isbn, liv_numero_paginas,
    liv_sinopse, liv_imagem_url, liv_codigo_barras, liv_valor_venda, liv_ativo,
    liv_altura, liv_largura, liv_profundidade, liv_peso, aut_id, edi_id, grp_pre_id
)
SELECT
    s.isbn,
    s.titulo,
    s.ano,
    1,
    s.isbn,
    s.paginas,
    s.sinopse,
    'https://covers.openlibrary.org/b/id/' || s.cover_id || '-L.jpg?default=false',
    s.isbn,
    s.valor,
    TRUE,
    s.altura,
    s.largura,
    s.profundidade,
    s.peso,
    (SELECT a.aut_id FROM autor a WHERE lower(trim(a.aut_nome)) = lower(trim(s.autor)) ORDER BY a.aut_id LIMIT 1),
    (SELECT e.edi_id FROM editora e WHERE lower(trim(e.edi_nome)) = lower(trim(s.editora)) ORDER BY e.edi_id LIMIT 1),
    (SELECT g.grp_pre_id FROM grupo_precificacao g WHERE g.grp_pre_nome = s.grupo)
FROM seed_livros_50 s
ON CONFLICT (liv_isbn) DO NOTHING;

-- Normaliza livros desta carga inseridos anteriormente com o prefixo SEED50-.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM livro antigo
        JOIN livro existente ON existente.liv_codigo = antigo.liv_isbn
                            AND existente.liv_id <> antigo.liv_id
        WHERE antigo.liv_isbn IN (SELECT isbn FROM seed_livros_50)
          AND antigo.liv_codigo = 'SEED50-' || antigo.liv_isbn
    ) THEN
        RAISE EXCEPTION 'Não foi possível normalizar liv_codigo: já existe outro livro usando o ISBN como código.';
    END IF;
END $$;

UPDATE livro l
SET liv_codigo = s.isbn
FROM seed_livros_50 s
WHERE l.liv_isbn = s.isbn
    AND l.liv_codigo = 'SEED50-' || s.isbn;

INSERT INTO livro_categoria (liv_id, cat_id)
SELECT l.liv_id, c.cat_id
FROM seed_livros_50 s
JOIN livro l ON l.liv_isbn = s.isbn
CROSS JOIN LATERAL unnest(s.categorias) AS categoria_seed(nome)
JOIN LATERAL (
    SELECT cat_id FROM categoria
    WHERE lower(trim(cat_nome)) = lower(trim(categoria_seed.nome))
    ORDER BY cat_id LIMIT 1
) c ON TRUE
ON CONFLICT (liv_id, cat_id) DO NOTHING;

INSERT INTO estoque (liv_id, est_quantidade_disponivel, est_quantidade_bloqueada, est_quantidade_vendida)
SELECT l.liv_id, s.disponivel, 0, 0
FROM seed_livros_50 s
JOIN livro l ON l.liv_isbn = s.isbn
ON CONFLICT (liv_id) DO NOTHING;

COMMIT;

-- Validação principal: uma linha por ISBN esperado, com autores, categorias e estoque.
SELECT
    l.liv_titulo AS titulo,
    l.liv_isbn AS isbn,
    l.liv_ano AS ano,
    a.aut_nome AS autor,
    e.edi_nome AS editora,
    string_agg(DISTINCT c.cat_nome, ', ' ORDER BY c.cat_nome) AS categorias,
    g.grp_pre_nome AS grupo_precificacao,
    g.grp_pre_percentual_margem AS margem_percentual,
    l.liv_valor_venda AS valor_venda,
    l.liv_imagem_url AS url_capa,
    est.est_quantidade_disponivel AS estoque_disponivel,
    s.edition_key AS fonte_edicao
FROM seed_livros_50 s
JOIN livro l ON l.liv_isbn = s.isbn
JOIN autor a ON a.aut_id = l.aut_id
JOIN editora e ON e.edi_id = l.edi_id
JOIN grupo_precificacao g ON g.grp_pre_id = l.grp_pre_id
LEFT JOIN livro_categoria lc ON lc.liv_id = l.liv_id
LEFT JOIN categoria c ON c.cat_id = lc.cat_id
LEFT JOIN estoque est ON est.liv_id = l.liv_id
GROUP BY l.liv_id, a.aut_nome, e.edi_nome, g.grp_pre_nome, g.grp_pre_percentual_margem,
         est.est_quantidade_disponivel, s.edition_key
ORDER BY l.liv_titulo;

-- Quantos dos 50 ISBNs da carga estão presentes.
SELECT count(DISTINCT l.liv_isbn) AS livros_da_carga_encontrados,
       50 AS quantidade_esperada
FROM seed_livros_50 s
JOIN livro l ON l.liv_isbn = s.isbn;

-- Livros da carga sem estoque.
SELECT s.titulo, s.isbn
FROM seed_livros_50 s
JOIN livro l ON l.liv_isbn = s.isbn
LEFT JOIN estoque est ON est.liv_id = l.liv_id
WHERE est.est_id IS NULL
ORDER BY s.titulo;

-- Livros da carga sem categoria.
SELECT s.titulo, s.isbn
FROM seed_livros_50 s
JOIN livro l ON l.liv_isbn = s.isbn
LEFT JOIN livro_categoria lc ON lc.liv_id = l.liv_id
WHERE lc.liv_id IS NULL
ORDER BY s.titulo;

-- Livros da carga sem URL de imagem.
SELECT s.titulo, s.isbn
FROM seed_livros_50 s
JOIN livro l ON l.liv_isbn = s.isbn
WHERE l.liv_imagem_url IS NULL OR trim(l.liv_imagem_url) = ''
ORDER BY s.titulo;
