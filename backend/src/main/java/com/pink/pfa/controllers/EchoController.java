// the endpoint must be contained in the same package or sub-package as the main class
// this is because the @SpringBootApplication flag scans for components, but only in
// same package or sub-package from which the flag is placed.
package com.pink.pfa.controllers;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pink.pfa.controllers.requests.EchoRequest;

@RestController
@RequestMapping("/api")
public class EchoController {
    // This tells Spring to answer POST requests with this method sent to this defined address
    @PostMapping("/echo")
    // @RequestBody tells Spring to create a java object (in this case an EchoRquest object) with the request body. This is how we can deserialize POST requests in Spring
    public Map<String, Object> echo (@RequestBody EchoRequest request) {
        return Map.of(
            "Original", request.getText(),
            "Length", request.getText().length()
        );
    }
}

// ---unix based os---
// curl -X POST http://localhost:8080/api/echo -H "Content-Type: application/json" -d '{"text":"hello spring"}'

// ---windows---
// iwr http://localhost:8080/api/echo -Method POST -Headers @{ "Content-Type" = "application/json" } -Body '{"text":"hello spring"}'
