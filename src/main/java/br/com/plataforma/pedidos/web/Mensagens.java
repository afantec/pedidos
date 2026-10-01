package br.com.plataforma.pedidos.web;

import br.com.plataforma.pedidos.comum.ConflitoException;
import br.com.plataforma.pedidos.comum.RecursoNaoEncontradoException;
import br.com.plataforma.pedidos.comum.RegraNegocioException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;

/** Mensagens das telas e tradução das exceções de serviço para o usuário. */
final class Mensagens {

    private Mensagens() {
    }

    static void info(String texto) {
        adicionar(FacesMessage.SEVERITY_INFO, texto);
    }

    static void erro(String texto) {
        adicionar(FacesMessage.SEVERITY_ERROR, texto);
        FacesContext.getCurrentInstance().validationFailed();   // mantém diálogos abertos
    }

    /** Mantém a mensagem após um redirect. */
    static void infoAposRedirect(String texto) {
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        info(texto);
    }

    /** Executa a ação e converte erros esperados em mensagem; devolve true se deu certo. */
    static boolean executar(Runnable acao) {
        try {
            acao.run();
            return true;
        } catch (RegraNegocioException | ConflitoException | RecursoNaoEncontradoException e) {
            erro(e.getMessage());
        } catch (ConstraintViolationException e) {
            e.getConstraintViolations().forEach(v -> erro(v.getPropertyPath() + ": " + v.getMessage()));
        } catch (OptimisticLockingFailureException e) {
            erro("O registro foi alterado por outro usuário. Recarregue a página.");
        } catch (DataIntegrityViolationException e) {
            erro("Operação viola a integridade dos dados.");
        }
        return false;
    }

    private static void adicionar(FacesMessage.Severity severidade, String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severidade, texto, null));
    }
}
