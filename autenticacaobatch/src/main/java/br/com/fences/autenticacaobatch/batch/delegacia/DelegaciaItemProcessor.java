package br.com.fences.autenticacaobatch.batch.delegacia;

import javax.batch.api.chunk.ItemProcessor;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import br.com.fences.autenticacaobatch.config.AppConfig;
import br.com.fences.autenticacaoentidade.rdo.delegacia.RdoDelegacia;
import br.com.fences.autenticacaoentidade.rdo.usuario.RdoUsuario;
import br.com.fences.fencesutils.conversor.converter.Converter;
import br.com.fences.fencesutils.rest.tratamentoerro.util.VerificarErro;
import br.com.fences.fencesutils.verificador.Verificador;


@Named
public class DelegaciaItemProcessor implements ItemProcessor{
	
	@Inject
	private transient Logger logger;
	
	@Inject
	private AppConfig appConfig;
	
	@Inject
	private VerificarErro verificarErro;

	@Inject
	private Converter<RdoDelegacia> rdoDelegaciaConverter;
//	
//	@Inject
//	private Converter<Indiciado> indiciadoConverter;
	
	private String host;
	private String port;
	
	@Override
	public RdoDelegacia processItem(Object item) throws Exception 
	{
		String idDelegacia = (String) item;
		
		host = appConfig.getServerBackendHost();
		port = appConfig.getServerBackendPort();
		
		logger.info("Extraindo... " + idDelegacia);
		Client client = ClientBuilder.newClient();
		String servico = "http://"
				+ host
				+ ":"
				+ port
				+ "/ocorrenciardobackend/rest/rdoextrair/"
				+ "consultarDelegacia/{idDelegacia}";
		WebTarget webTarget = client.target(servico);
		Response response = webTarget
				.resolveTemplate("idDelegacia", idDelegacia)
				.request(MediaType.APPLICATION_JSON)
				.get();
		String json = response.readEntity(String.class);
		if (verificarErro.contemErro(response, json))
		{
			String msg = verificarErro.criarMensagem(response, json, servico);
			logger.error(msg);
			throw new RuntimeException(msg);
		}
		RdoDelegacia rdoDelegacia = rdoDelegaciaConverter.paraObjeto(json, RdoDelegacia.class);
		return rdoDelegacia;
	}
}
