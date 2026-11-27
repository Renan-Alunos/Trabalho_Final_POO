// Padrao Observer: quem quiser ser avisado das mudancas do Sistema implementa esta interface
// e se registra com sistema.adicionarObservador(...).
public interface ObservadorSistema {
    void atualizar(EventoSistema evento, Object dados);
}
