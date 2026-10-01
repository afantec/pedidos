package br.com.plataforma.pedidos.cliente;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Page<Cliente> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    List<Cliente> findByAtivoTrue(Sort sort);

    boolean existsByDocumentoAndIdNot(String documento, Long id);

    boolean existsByDocumento(String documento);
}
