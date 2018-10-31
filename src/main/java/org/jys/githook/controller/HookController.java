package org.jys.githook.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import java.io.File;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * common web hook controller
 * please use corrent hook mapping for specific git repo service
 * repo in the url path for identify different repo event
 * put your shell script in ${SCRIPT_PATH}/repo/ path
 * and it will execute script with same name as event name
 * such as "push" event will execute "push.sh" in ${SCRIPT_PATH}/repo/
 * remeber to add "chmod +x" permission for you script
 * @author: JiangYueSong
*/
@RestController
@RequestMapping(value="/hook")
public class HookController{

    @Value("${github.token")
    private String GITHUB_TOKEN;

    @Value("${gitee.token")
    private String GITEE_TOKEN;

    @Value("${coding.token")
    private String CODING_TOKEN;

    @Value("${script.path")
    private String SCRIPT_PATH;

    @PostMapping(value="/github/{repo}")
    public void githubHook(@RequestHeader(value="User-Agent") String agent,
                            @RequestHeader(value="X-Github-Event") String event,
                            @RequestHeader(value="X-Hub-Signature",required=false)String signature,
                            @PathVariable(value="repo")String repo) throws IOException{
        if(!agent.contains("Github-Hookshot")){
            throw new RuntimeException();        
        }

        //check signature to secure your 
        if(signature!=null){

        }
        
        File script=Paths.get(SCRIPT_PATH,repo,event+".sh").toFile();
        if(!script.exists()){
            throw new RuntimeException();
        }
        ProcessBuilder builder=new ProcessBuilder(script.toString());
        builder.start();
    }

    @PostMapping(value="/gitee/{repo}")
    public void giteeHook(@RequestHeader(value="User-Agent") String agent,
                            @RequestHeader(value="X-Gitee-Event") String event,
                            @RequestHeader(value="X-Gitee-Token",required=false)String signature,
                            @PathVariable(value="repo")String repo)throws IOException{
        
        if(Objects.equals("git-oschina-hook",agent)){
            throw new RuntimeException();        
        }

        //check signature to secure your 
        if(signature!=null && !Objects.equals(GITEE_TOKEN,signature)){
            
        }
        
        File script=Paths.get(SCRIPT_PATH,repo,event+".sh").toFile();
        if(!script.exists()){
            throw new RuntimeException();
        }
        ProcessBuilder builder=new ProcessBuilder(script.toString());
        builder.start();
    }

    @PostMapping(value="/coding/{repo}")
    public void codingHook(@RequestHeader(value="User-Agent") String agent,
                            @RequestHeader(value="X-Coding-Event") String event,                            
                            @PathVariable(value="repo")String repo,
                            @RequestBody Map<String,Object> body)throws IOException{
        
        if(Objects.equals("Coding.net Hook",agent)){
            throw new RuntimeException();        
        }

        //check signature to secure your 
        String signature=(String)body.get("token");
        if(signature!=null && !Objects.equals(CODING_TOKEN,signature)){
            throw new RuntimeException();
        }
        
        File script=Paths.get(SCRIPT_PATH,repo,event+".sh").toFile();
        if(!script.exists()){
            throw new RuntimeException();
        }
        ProcessBuilder builder=new ProcessBuilder(script.toString());
        builder.start();
    }
}