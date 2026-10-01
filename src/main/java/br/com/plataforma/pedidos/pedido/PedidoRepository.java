package br.com.plataforma.pedidos.pedido;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Query(value = """
            select p from Pedido p join fetch p.cliente c
             where (:status is null or p.status = :status)
               and (:clienteId is null or c.id = :clienteId)
            """,
            countQuery = """
            select count(p) from Pedido p
             where (:status is null or p.status = :status)
               and (:clienteId is null or p.cliente.id = :clienteId)
            """)
    Page<Pedido> filtrar(@Param("status") StatusPedido status,
                         @Param("clienteId") Long clienteId,
                         Pageable pageable);

    /** Pedido com cliente, itens e produtos carregados (open-in-view está desligado). */
    @Query("""
            select distinct p from Pedido p
              join fetch p.cliente
              left join fetch p.itens i
              left join fetch i.produto
             where p.id = :id
            """)
    Optional<Pedido> buscarCompleto(@Param("id") Long id);

    long countByStatus(StatusPedido status);

    boolean existsByClienteId(Long clienteId);

    @Query("select count(i) > 0 from ItemPedido i where i.produto.id = :produtoId")
    boolean existeItemComProduto(@Param("produtoId") Long produtoId);
}
