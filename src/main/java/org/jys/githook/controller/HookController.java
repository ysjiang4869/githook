package org.jys.githook.controller;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import java.io.File;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * common web hook controller
 * please use current hook mapping for specific git repo service
 * repo in the url path for identify different repo event
 * put your shell script in ${scriptPath}/repo/ path
 * and it will execute script with same name as event name
 * such as "push" event will execute "push.sh" in ${scriptPath}/repo/
 * remember to add "chmod +x" permission for you script
 * @author JiangYueSong
*/
@RestController
@RequestMapping(value="/hook")
public class HookController{

    @Value("${github.token")
    private String githubToken;

    @Value("${gitee.token")
    private String giteeToken;

    @Value("${coding.token")
    private String codingToken;

    @Value("${script.path")
    private String scriptPath;

    private static final String GITHUB_AGENT="Github-Hookshot";
    private static final String GITEE_AGENT="git-oschina-hook";
    private static final String CODING_AGENT="Coding.net Hook";

    private static final Logger logger= LoggerFactory.getLogger(HookController.class);

    /**
     * handle github web hook event
     * @param agent request agent
     * @param event web hook event
     * @param signature github signature
     * @param repo repo name
     * @param body request body
     */
    @PostMapping(value="/github/{repo}")
    public void githubHook(@RequestHeader(value="User-Agent") String agent,
                           @RequestHeader(value="X-Github-Event") String event,
                           @RequestHeader(value="X-Hub-Signature",required=false)String signature,
                           @PathVariable(value="repo")String repo,
                           @RequestBody String body){
        if(!agent.contains(GITHUB_AGENT)){
            logger.error("request not form github");
            throw new RuntimeException();        
        }

        //check signature to secure your app
        if(signature!=null){
            byte[] sha1Bytes=new HmacUtils(HmacAlgorithms.HMAC_SHA_1, githubToken).hmac(body);
            String validation="sha1="+ Hex.encodeHexString(sha1Bytes);
            if(!Objects.equals(validation,signature)){
                logger.error("signature doesn't match !");
                throw new RuntimeException();
            }
        }

        execute(repo,event);
    }

    /**
     * handle gitee web hook event
     * @param agent request agent
     * @param event web hook event
     * @param signature gitee secret
     * @param repo repo name
     */
    @PostMapping(value="/gitee/{repo}")
    public void giteeHook(@RequestHeader(value="User-Agent") String agent,
                          @RequestHeader(value="X-Gitee-Event") String event,
                          @RequestHeader(value="X-Gitee-Token",required=false)String signature,
                          @PathVariable(value="repo")String repo){
        
        if(Objects.equals(GITEE_AGENT,agent)){
            logger.error("request not form gitee");
            throw new RuntimeException();        
        }

        //check signature to secure your app
        if(signature!=null && !Objects.equals(giteeToken,signature)){
            logger.error("signature doesn't match !");
            throw new RuntimeException();
        }

        execute(repo,event);
    }

    /**
     * handle coding.net web hook event
     * @param agent request agent
     * @param event web hook event
     * @param repo repo name
     */
    @PostMapping(value="/coding/{repo}")
    public void codingHook(@RequestHeader(value="User-Agent") String agent,
                           @RequestHeader(value="X-Coding-Event") String event,
                           @PathVariable(value="repo")String repo,
                           @RequestBody Map<String,Object> body){
        
        if(Objects.equals(CODING_AGENT,agent)){
            logger.error("request not form coding");
            throw new RuntimeException();        
        }

        //check signature to secure your app
        String signature=(String)body.get("token");
        if(signature!=null && !Objects.equals(codingToken,signature)){
            logger.error("signature doesn't match !");
            throw new RuntimeException();
        }
        execute(repo,event);
    }

    private void execute(String repo, String event){
        File script=Paths.get(scriptPath,repo,event+".sh").toFile();
        String path=script.toString();
        if(!script.exists()){
            logger.error("script file {} not exist",path);
            throw new RuntimeException();
        }
        ProcessBuilder builder=new ProcessBuilder(path);
        try {
            builder.start();
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }
    }
}