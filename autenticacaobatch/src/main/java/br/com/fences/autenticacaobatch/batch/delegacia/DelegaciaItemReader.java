package br.com.fences.autenticacaobatch.batch.delegacia;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.batch.api.chunk.AbstractItemReader;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import br.com.fences.autenticacaobatch.config.AppConfig;
import br.com.fences.fencesutils.conversor.converter.ColecaoJsonAdapter;
import br.com.fences.fencesutils.rest.tratamentoerro.util.VerificarErro;


@Named
public class DelegaciaItemReader extends AbstractItemReader {

	@Inject 
	private transient Logger logger;
	
	@Inject
	private AppConfig appConfig;
	
	@Inject
	private VerificarErro verificarErro;
	
	
	private Gson gson = new GsonBuilder()
			.registerTypeHierarchyAdapter(Collection.class, new ColecaoJsonAdapter())
			.create();
	
	private Iterator<String> iteratorDelegacias;
	
	private String host;
	private String port;
	
	@Override
	public void open(Serializable checkpoint) throws Exception {
		
		host = appConfig.getServerBackendHost();
		port = appConfig.getServerBackendPort();

		
		logger.info("Recuperar lista de id de delegacias do RDO...");
		Client client = ClientBuilder.newClient();
		String servico = "http://" + host + ":"+ port + "/ocorrenciardobackend/rest/rdoextrair/" + 
				"listarDelegacias";
		WebTarget webTarget = client
				.target(servico);
		Response response = webTarget
				.request(MediaType.APPLICATION_JSON)
				.get();
		String json = response.readEntity(String.class);
		if (verificarErro.contemErro(response, json))
		{
			String msg = verificarErro.criarMensagem(response, json, servico);
			logger.error(msg); 
			throw new RuntimeException(msg);
		}
		
		ListaDelegacias listaDelegacias = gson.fromJson(json, ListaDelegacias.class);
		iteratorDelegacias =  listaDelegacias.getDelegacias().iterator();
		logger.info("Foram lidos [" + listaDelegacias.getDelegacias().size() + "] registros de id de delegacias para carga.");
		
	}
	
	/**
	 * O container ira parar de chamar esse metodo quando retornar nulo.
	 */
	@Override
	public String readItem() throws Exception 
	{
		String idDelegacia = null;
		if (iteratorDelegacias.hasNext())
		{
			idDelegacia = iteratorDelegacias.next();
		}
		if (idDelegacia == null)
		{
			logger.info("Nao existe mais registro para leitura. Termino do Job.");
		}
		
		return idDelegacia;
	}
	
	
	public class ListaDelegacias{
		
		@SerializedName("ID_DELEGACIA")
		private List<String> delegacias = new ArrayList<>();

		public List<String> getDelegacias() {
			return delegacias;
		}

		public void setDelegacias(List<String> delegacias) {
			this.delegacias = delegacias;
		}
	}

}
