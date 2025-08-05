package app.core.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserResource {

    @GetMapping
    public UserResult getUser() throws Exception {
        return new UserResult(SecurityUtils.currentUserName(), SecurityUtils.currentUserRoles());
    }

}
