package br.com.plataforma.pedidos.produto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    @Query("""
            select p from Produto p
             where lower(p.codigo) like lower(concat('%', :termo, '%'))
                or lower(p.descricao) like lower(concat('%', :termo, '%'))
            """)
    Page<Produto> pesquisar(@Param("termo") String termo, Pageable pageable);

    List<Produto> findByAtivoTrue(Sort sort);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);
}
