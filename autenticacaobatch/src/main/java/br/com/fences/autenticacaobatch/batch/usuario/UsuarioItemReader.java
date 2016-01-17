package br.com.fences.autenticacaobatch.batch.usuario;

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
public class UsuarioItemReader extends AbstractItemReader {

	@Inject 
	private transient Logger logger;
	
	@Inject
	private AppConfig appConfig;
	
	@Inject
	private VerificarErro verificarErro;
	
	
	private Gson gson = new GsonBuilder()
			.registerTypeHierarchyAdapter(Collection.class, new ColecaoJsonAdapter())
			.create();
	
	private Iterator<String> iteratorUsuarios;
	
	private String host;
	private String port;
	
	@Override
	public void open(Serializable checkpoint) throws Exception {
		
		host = appConfig.getServerBackendHost();
		port = appConfig.getServerBackendPort();

		
		logger.info("Recuperar lista de rg de usuarios do RDO...");
		Client client = ClientBuilder.newClient();
		String servico = "http://" + host + ":"+ port + "/ocorrenciardobackend/rest/rdoextrair/" + 
				"listarUsuarios";
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
		
		ListaUsuarios listaUsuarios = gson.fromJson(json, ListaUsuarios.class);
		iteratorUsuarios =  listaUsuarios.getUsuarios().iterator();
		logger.info("Foram lidos [" + listaUsuarios.getUsuarios().size() + "] registros de rg de usuarios para carga.");
		
	}
	
	/**
	 * O container ira parar de chamar esse metodo quando retornar nulo.
	 */
	@Override
	public String readItem() throws Exception 
	{
		String rgUsuario = null;
		if (iteratorUsuarios.hasNext())
		{
			rgUsuario = iteratorUsuarios.next();
		}
		if (rgUsuario == null)
		{
			logger.info("Nao existe mais registro para leitura. Termino do Job.");
		}
		
		return rgUsuario;
	}
	
	
	public class ListaUsuarios{
		
		@SerializedName("RG_USUARIO")
		private List<String> usuarios = new ArrayList<>();

		public List<String> getUsuarios() {
			return usuarios;
		}

		public void setUsuarios(List<String> usuarios) {
			this.usuarios = usuarios;
		}
	}

}
