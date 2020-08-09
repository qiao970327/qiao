package com.fh.shop.job;


import com.fh.shop.product.biz.ProductService;
import com.fh.shop.utils.MailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Component
public class StockJob {
    @Resource(name = "productService")
    private ProductService productService;
    @Autowired
    private MailUtil mailUtil;

    /*@Scheduled(cron = "0 0/3 * * * ?")
    //@Scheduled(fixedRate = 2000*30*3)
    public void a(){
        List<Product> list = productService.findProduct();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"1\">\n" +
                "     <thead>\n" +
                "         <tr>\n" +
                "             <th>商品名</th>\n" +
                "             <th>价格</th>\n" +
                "             <th>库存</th>\n" +
                "         </tr>\n" +
                "     </thead>\n" +
                "    <tbody>"
        );
        for (Product product : list) {
            stringBuffer.append("<tr>\n" +
                    "        <td>"+product.getProductName()+"</td>\n" +
                    "        <td>"+product.getPrice().toString()+"</td>\n" +
                    "        <td>"+product.getStock()+"</td>\n" +
                    "    </tr>");
        }
        stringBuffer.append("</tbody>\n" +
                "</table>");
        String s = stringBuffer.toString();
        try {
            mailUtil.DaoMail("@qq.com",s,
                    "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
    }

