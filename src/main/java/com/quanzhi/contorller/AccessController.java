package com.quanzhi.contorller;

import com.quanzhi.client.NifiClient;
import com.quanzhi.service.NifiService;
import com.quanzhi.vo.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author：CatTail
 * @date: 2024/6/13
 * @Copyright: https://github.com/CatTailzz
 */
@RestController
@RequestMapping("/nifi")
public class AccessController {

    @Autowired
    private NifiService nifiService;

    @GetMapping("/process-groups/getGroupsList")
    public ResponseVo getProcessGroups() {
        return nifiService.getProcessGroups();
    }

    @PutMapping("/process-groups/start/{processGroupId}")
    public void startProcessGroup(@PathVariable String processGroupId) throws Exception {
        nifiService.startProcessGroup(processGroupId);
    }

    @PutMapping("/process-groups/stop/{processGroupId}")
    public void stopProcessGroup(@PathVariable String processGroupId) throws Exception {
        nifiService.stopProcessGroup(processGroupId);
    }

    @GetMapping("/process-groups/status/{processGroupId}")
    public ResponseVo processGroupHealth(@PathVariable String processGroupId) throws Exception {
        return nifiService.ProcessGroupHealth(processGroupId);
    }
}
