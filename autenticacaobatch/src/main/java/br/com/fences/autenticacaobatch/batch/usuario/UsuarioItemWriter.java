package br.com.fences.autenticacaobatch.batch.usuario;

import java.util.List;

import javax.batch.api.chunk.AbstractItemWriter;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import br.com.fences.autenticacaobatch.config.AppConfig;
import br.com.fences.autenticacaoentidade.rdo.delegacia.RdoDelegacia;
import br.com.fences.autenticacaoentidade.rdo.usuario.RdoUsuario;
import br.com.fences.fencesutils.conversor.converter.Converter;
import br.com.fences.fencesutils.rest.tratamentoerro.util.VerificarErro;

@Named
public class UsuarioItemWriter extends AbstractItemWriter{

	@Inject
	private transient Logger logger;
	
	@Inject
	private AppConfig appConfig;
	
	@Inject
	private VerificarErro verificarErro;
	
	@Inject
	private Converter<RdoUsuario> rdoUsuarioConverter;
	
	private String host;
	private String port;
	
	@Override
	public void writeItems(List<Object> items) throws Exception {
		
		host = appConfig.getServerBackendHost();
		port = appConfig.getServerBackendPort();
		
		for (Object item : items)
		{
			RdoUsuario rdoUsuario = (RdoUsuario) item;

			String json = rdoUsuarioConverter.paraJson(rdoUsuario);
			Client client = ClientBuilder.newClient();
		
			String servico = "http://" + host + ":"+ port + "/autenticacaobackend/rest/" + 
					"rdoUsuario/adicionarOuAtualizar";
			WebTarget webTarget = client.target(servico);
			Response response = webTarget	
					.request(MediaType.APPLICATION_JSON)
					.post(Entity.json(json));
			json = response.readEntity(String.class);
			if (verificarErro.contemErro(response, json))
			{
				String msg =  verificarErro.criarMensagem(response, json, servico);
				logger.error(msg);
				throw new RuntimeException(msg);
			}
		}
		
	}
}
