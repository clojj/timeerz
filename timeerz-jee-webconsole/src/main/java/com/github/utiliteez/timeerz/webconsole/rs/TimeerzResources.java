package com.github.utiliteez.timeerz.webconsole.rs;

import com.github.utiliteez.timeerz.core.TimerObject;
import com.github.utiliteez.timeerz.jee.TimeerzManager;
import com.github.utiliteez.timeerz.webconsole.model.TimerInfoMessage;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Path("timeerz")
public class TimeerzResources {

    @Inject
    private TimeerzManager timeerzManager;

    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TimerInfoMessage> list() throws WebApplicationException {
        return timeerzManager.listAllActive().stream().map(TimeerzResources::convert).collect(Collectors.toCollection(ArrayList::new));
    }

    @NotNull
    private static TimerInfoMessage convert(TimerObject timerObject) {
        return new TimerInfoMessage(timerObject.getId(), timerObject.toString());
    }
}