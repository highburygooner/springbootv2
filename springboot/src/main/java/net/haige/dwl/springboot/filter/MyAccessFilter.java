package net.haige.dwl.springboot.filter;

import org.apache.shiro.web.filter.AccessControlFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MyAccessFilter extends AccessControlFilter {

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue)
            throws Exception {
        System.out.println("accessFilter");
        System.out.println("isAuthenticated1");
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        System.out.println(this.getPathWithinApplication(request));
        String viewUrl = this.getPathWithinApplication(request);
        boolean res=this.getSubject(httpServletRequest, response).isAuthenticated();
        System.out.println(res);
        return getSubject(request, response).isPermitted(viewUrl);
        // TODO Auto-generated method stub
        //return true;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        // TODO Auto-generated method stub
        response401(request,response);

        return false;
    }
    /**
     * 将请求返回到 /401
     */
    private void response401(ServletRequest req, ServletResponse resp) throws Exception {
        HttpServletResponse httpServletResponse = (HttpServletResponse) resp;
        httpServletResponse.sendRedirect("/401");
    }


}
