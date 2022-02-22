package com.botmultilaser.bot.service;

import com.botmultilaser.bot.config.BotDados;
import com.botmultilaser.bot.model.request.AvaliacaoRequest;
import com.botmultilaser.bot.model.request.SuporteRequest;
import com.botmultilaser.bot.model.response.ProdutoResponse;
import com.botmultilaser.bot.model.response.SuporteResponse;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.URI;
import java.net.URISyntaxException;

import static org.springframework.http.MediaType.APPLICATION_JSON;

public class EchoBot extends TelegramLongPollingBot {


    private String suporteApi = "http://localhost:8080/rest";

    public String cliente;
    public String produto;
    public Long produtoId;
    public Long duvida;


    @Override
    public String getBotUsername() {
        return BotDados.BOT_USER_NAME;
    }

    @Override
    public String getBotToken() {
        return BotDados.BOT_TOKEN;
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            var mensagem = responder(update);
            try {
                execute(mensagem);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }


    private SendMessage responder(Update update) throws URISyntaxException {
        var textoMensagem = update.getMessage().getText().toLowerCase();
        var chatId = update.getMessage().getChatId().toString();

        var resposta = "";

        if ( textoMensagem.startsWith("/start") || textoMensagem.startsWith("ola") || textoMensagem.startsWith("olá")  || textoMensagem.startsWith("oi")) {
            this.cliente = null;
            this.produto = null;
            this.duvida = null;
            this.produtoId = null;

            resposta = "Olá, sou um bot de suporte para o seu produto Multilaser.\nPor favor, informe seu nome prosseguirmos!";
        }else if (this.cliente == null & !textoMensagem.isEmpty()) {
            this.cliente = textoMensagem;
            resposta = "É um prazer te conhecer "+this.cliente+"! Agora me informe o código do produto para prosseguirmos.";
        }else if ( this.produto == null & !textoMensagem.isEmpty() & this.cliente != null ) {

            ProdutoResponse produtoResponse = getProduto(textoMensagem);
            if( this.produto == null ){
                this.produto = produtoResponse.getNome();
                this.produtoId = produtoResponse.getId();
                resposta = "Agora me informe sua dúvida para o produto,"+this.produto+ ", "+ this.cliente;
            }else {
                resposta = "Desculpe, Não achamos o seu produto em nosso sistema, verifique o código do produto e tente novamente!";
            }

        } else if (this.duvida == null & !textoMensagem.isEmpty() & this.cliente != null & this.produto != null  ) {

            SuporteResponse response =  getSearch(textoMensagem, this.produto);
            if(this.duvida == null){
                this.duvida = response.getId();
                resposta = response.getResposta()+ " Para finalizarmos, poderia avaliar nosso atendimento automatico com uma nota de 1 a 10, digitando apenas números.";
            }

        }else if(this.duvida != null & Long.parseLong(textoMensagem)  >= 1 || Long.parseLong(textoMensagem) <=10 ){

            postAvaliacao(Long.parseLong(textoMensagem),this.cliente,this.duvida);
            resposta = "Obrigado pela avaliação! Agradecemos pelo seu contato";
        }

        return SendMessage.builder()
                .text(resposta)
                .chatId(chatId)
                .build();
    }


    public SuporteResponse getSearch(String pergunta,String produto)  {

        RestTemplate restTemplate = new RestTemplate();
        SuporteRequest request = new SuporteRequest();
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(APPLICATION_JSON);

        request.setProduto(produto);
        request.setDuvida(pergunta);

        HttpEntity<SuporteRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<SuporteResponse> result = restTemplate.exchange( suporteApi+"/faq/ajuda", HttpMethod.POST, entity, SuporteResponse.class );

        return  result.getBody();
    }


    public ProdutoResponse getProduto(String produto)  {

        HttpHeaders httpHeaders = new HttpHeaders();
        RestTemplate restTemplate = new RestTemplate();
        httpHeaders.setContentType(APPLICATION_JSON);
        ProdutoResponse produtoResponse = new ProdutoResponse();

        produtoResponse = restTemplate.getForObject(suporteApi+"/faq/produto/"+produto , ProdutoResponse.class);
        return produtoResponse ;
    }

    public URI postAvaliacao(Long avaliacao, String cliente, Long duvida){

        RestTemplate restTemplate = new RestTemplate();
        AvaliacaoRequest request = new AvaliacaoRequest();
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(APPLICATION_JSON);

        request.setAvaliacao(avaliacao);
        request.setNome(cliente);
        request.setPergunta(duvida);

        HttpEntity<AvaliacaoRequest> entity = new HttpEntity<>(request, headers);
        var result = restTemplate.postForLocation(suporteApi+"/cliente",request);
        return result;

    }



}
