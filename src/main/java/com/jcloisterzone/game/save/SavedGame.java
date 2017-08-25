package com.jcloisterzone.game.save;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jcloisterzone.Application;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.wsio.message.WsReplayableMessage;


public class SavedGame implements Serializable {

    private static final long serialVersionUID = 1L;

    private String gameId;
    private String name;
    private String version;
    private Date created;
    private MutableGameSetup setup;
    private List<WsReplayableMessage> replay;

    public SavedGame() {
    }

    public SavedGame(Game game) {
        gameId = game.getGameId();
        name = game.getName();
        version = Application.VERSION;
        created = new Date();
        setup = new MutableGameSetup();
        setup.setExpansions(game.getSetup().getExpansions().toJavaSet());
        setup.setRules(game.getSetup().getRules().toJavaMap());
        replay = game.getReplay().reverse().toJavaList();
    }


    public MutableGameSetup getSetup() {
        return setup;
    }

    public void setSetup(MutableGameSetup setup) {
        this.setup = setup;
    }

    public List<WsReplayableMessage> getReplay() {
        return replay;
    }

    public void setReplay(List<WsReplayableMessage> replay) {
        this.replay = replay;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    class MutableGameSetup {
        private Set<Expansion> expansions;
        private Map<CustomRule, Object> rules;
        public Set<Expansion> getExpansions() {
            return expansions;
        }
        public void setExpansions(Set<Expansion> expansions) {
            this.expansions = expansions;
        }
        public Map<CustomRule, Object> getRules() {
            return rules;
        }
        public void setRules(Map<CustomRule, Object> rules) {
            this.rules = rules;
        }
    }
}
