package com.huawei.test.myraspweb.controller;

import com.huawei.test.myraspweb.bean.ReportBean;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
public class ReportController {

    List<ReportBean> reportBeanList = new ArrayList<>();

    @RequestMapping("/report")
    public Map reportAnIssue(@RequestBody ReportBean reportBean)
    {
        Map map = new LinkedHashMap();
        reportBeanList.add(reportBean);
        map.put("success", true);
        return map;
    }

    @RequestMapping("/issue")
    public Map getIssueByPage(String type, int page, int limit)
    {
        Map map = new HashMap();
        page -= 1;
        List<ReportBean> collect = reportBeanList.stream().filter(item1 -> item1.getType().equals(type)).collect(Collectors.toList());
        int start = page * limit;
        int end = (page + 1) * limit;
        if (start < collect.size()) {
            end = collect.size() > end ? end : collect.size();
            map.put("code", 0);
            map.put("msg", "");
            map.put("count", collect.size());
            map.put("data",collect.subList(start, end));
        }
        else {
            map.put("code", 1);
            map.put("msg", "");
            map.put("count", 0);
            map.put("data",null);
        }
        return map;
    }
}
