package com.livrofacil.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component
public class CheckoutSchemaMigration {
  private static final Logger LOGGER = LoggerFactory.getLogger(CheckoutSchemaMigration.class);
    private final JdbcTemplate jdbcTemplate;

    public CheckoutSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void ajustarConstraintsDoCheckout() {
      jdbcTemplate.execute("alter table cliente add column if not exists cli_data_exclusao timestamp");
      jdbcTemplate.execute("alter table cliente add column if not exists cli_dados_anonimizados boolean not null default false");
      jdbcTemplate.execute("update cliente set cli_dados_anonimizados = false where cli_dados_anonimizados is null");
      jdbcTemplate.execute("alter table cliente add column if not exists cli_numero_registro bigint");
      jdbcTemplate.execute("update cliente set cli_numero_registro = seq.numero from (select cli_id, row_number() over (order by cli_id) as numero from cliente where cli_numero_registro is null) seq where cliente.cli_id = seq.cli_id");
      jdbcTemplate.execute("alter table cliente alter column cli_numero_registro set not null");
      jdbcTemplate.execute("create unique index if not exists uk_cliente_numero_registro on cliente (cli_numero_registro)");
      jdbcTemplate.execute("alter table forma_pagamento add column if not exists for_pag_criado_em timestamp");
      jdbcTemplate.execute("update forma_pagamento set for_pag_criado_em = current_timestamp where for_pag_criado_em is null");
      jdbcTemplate.execute("alter table forma_pagamento alter column for_pag_criado_em set default current_timestamp");
      jdbcTemplate.execute("alter table forma_pagamento alter column for_pag_criado_em set not null");

        List<String> constraints = jdbcTemplate.queryForList("""
                select distinct c.conname
                from pg_constraint c
                join pg_class t on t.oid = c.conrelid
                join pg_attribute a on a.attrelid = t.oid and a.attnum = any(c.conkey)
                where t.relname = 'pedido'
                  and c.contype = 'u'
                  and a.attname = 'ped_checkout_chave'
                """, String.class);

        constraints.forEach(nome -> jdbcTemplate.execute(
            "alter table pedido drop constraint if exists \"" + nome.replace("\"", "\"\"") + "\""));
        if (!constraints.isEmpty()) {
          LOGGER.info("Constraints antigas de checkout removidas de pedido: {}", constraints);
        }

        // A constraint antiga pode aceitar apenas os status originais do pedido.
        // Os status do checkout são controlados pelo enum StatusPedido.
        jdbcTemplate.execute("alter table pedido drop constraint if exists pedido_ped_status_check");

        jdbcTemplate.queryForList("""
            select c.conname
            from pg_constraint c
            join pg_class t on t.oid = c.conrelid
            where t.relname = 'pedido'
              and c.contype = 'c'
              and pg_get_constraintdef(c.oid) ilike '%ped_status%'
            """, String.class).forEach(nome -> jdbcTemplate.execute(
            "alter table pedido drop constraint if exists \"" + nome.replace("\"", "\"\"") + "\""));

        // Regra legada: o item atual referencia o livro e nao possui titulo proprio.
        Integer tituloRemovido = jdbcTemplate.queryForObject("""
          select count(*)
          from pg_constraint c
          join pg_class t on t.oid = c.conrelid
          where t.relname = 'item_pedido'
            and c.conname = 'ite_ped_titulo'
          """, Integer.class);
        jdbcTemplate.execute("alter table item_pedido drop constraint if exists ite_ped_titulo");
        if (tituloRemovido != null && tituloRemovido > 0) {
            LOGGER.info("Constraint legada ite_ped_titulo removida de item_pedido");
        }

        jdbcTemplate.queryForList("""
            select c.conname
            from pg_constraint c
            join pg_class t on t.oid = c.conrelid
            where t.relname = 'item_pedido'
              and c.contype = 'c'
              and pg_get_constraintdef(c.oid) ilike '%titulo%'
            """, String.class).forEach(nome -> jdbcTemplate.execute(
            "alter table item_pedido drop constraint if exists \"" + nome.replace("\"", "\"\"") + "\""));

            adicionarConstraint("item_carrinho", "uk_item_carrinho_carrinho_livro",
              "UNIQUE (car_id, liv_id)");
            adicionarConstraint("item_carrinho", "ck_item_carrinho_quantidade_positiva",
              "CHECK (ite_car_quantidade > 0)");
            adicionarConstraint("item_pedido", "ck_item_pedido_quantidade_positiva",
              "CHECK (ite_ped_quantidade > 0)");
            adicionarConstraint("item_pedido", "ck_item_pedido_valor_nao_negativo",
              "CHECK (ite_ped_valor_unitario >= 0)");
            adicionarConstraint("pedido", "ck_pedido_subtotal_nao_negativo",
              "CHECK (ped_subtotal >= 0)");
            adicionarConstraint("pedido", "ck_pedido_desconto_nao_negativo",
              "CHECK (ped_desconto >= 0)");
            adicionarConstraint("pedido", "ck_pedido_total_nao_negativo",
              "CHECK (ped_total >= 0)");
            adicionarConstraint("pagamento_pedido", "ck_pagamento_valor_positivo",
              "CHECK (pag_ped_valor > 0)");
            adicionarConstraint("pagamento_pedido", "ck_pagamento_parcelas_validas",
              "CHECK (pag_ped_parcelas BETWEEN 1 AND 12)");
            adicionarConstraint("estoque", "ck_estoque_disponivel_nao_negativo",
              "CHECK (est_quantidade_disponivel >= 0)");
            adicionarConstraint("estoque", "ck_estoque_bloqueado_nao_negativo",
              "CHECK (est_quantidade_bloqueada >= 0)");
            adicionarConstraint("estoque", "ck_estoque_vendido_nao_negativo",
              "CHECK (est_quantidade_vendida >= 0)");
    }

              private void adicionarConstraint(String tabela, String nome, String definicao) {
            Integer existente = jdbcTemplate.queryForObject("""
              select count(*)
              from pg_constraint c
              join pg_class t on t.oid = c.conrelid
              where t.relname = ?
                and c.conname = ?
              """, Integer.class, tabela, nome);
            if (existente != null && existente == 0) {
                jdbcTemplate.execute("alter table \"" + tabela + "\" add constraint \"" + nome + "\" " + definicao);
                LOGGER.info("Constraint criada: {}.{}", tabela, nome);
            }
              }
}
