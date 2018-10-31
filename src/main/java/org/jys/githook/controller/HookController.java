package org.jys.githook.controller
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value="/hook")
public class HookController{

    @PostMapping(value="/github/{script}")
    public void githubHook(){

    }

    @PostMapping(value="/gitee/{script}")
    public void giteeHook(){

    }
}