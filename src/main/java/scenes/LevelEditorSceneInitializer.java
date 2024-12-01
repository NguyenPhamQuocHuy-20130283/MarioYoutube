package scenes;

import components.*;
import imgui.ImGui;
import imgui.ImVec2;
import jade.*;
import org.joml.Vector2f;
import physics2d.components.Box2DCollider;
import physics2d.components.Rigidbody2D;
import physics2d.enums.BodyType;
import util.AssetPool;

import java.io.File;
import java.util.Collection;
import java.util.function.Supplier;

public class LevelEditorSceneInitializer extends SceneInitializer {

    private Spritesheet sprites;
    private GameObject levelEditorStuff;

    public LevelEditorSceneInitializer() {

    }

    @Override
    public void init(Scene scene) {
        sprites = AssetPool.getSpritesheet("assets/images/spritesheets/decorationsAndBlocks.png");
        Spritesheet gizmos = AssetPool.getSpritesheet("assets/images/gizmos.png");

        levelEditorStuff = scene.createGameObject("LevelEditor");
        levelEditorStuff.setNoSerialize();
        levelEditorStuff.addComponent(new MouseControls());
        levelEditorStuff.addComponent(new KeyControls());
        levelEditorStuff.addComponent(new GridLines());
        levelEditorStuff.addComponent(new EditorCamera(scene.camera()));
        levelEditorStuff.addComponent(new GizmoSystem(gizmos));
        scene.addGameObjectToScene(levelEditorStuff);
    }

    @Override
    public void loadResources(Scene scene) {
        AssetPool.getShader("assets/shaders/default.glsl");

        AssetPool.addSpritesheet("assets/images/spritesheets/decorationsAndBlocks.png",
                new Spritesheet(AssetPool.getTexture("assets/images/spritesheets/decorationsAndBlocks.png"),
                        16, 16, 81, 0));
        AssetPool.addSpritesheet("assets/images/spritesheet.png",
                new Spritesheet(AssetPool.getTexture("assets/images/spritesheet.png"),
                        16, 16, 26, 0));
        AssetPool.addSpritesheet("assets/images/turtle.png",
                new Spritesheet(AssetPool.getTexture("assets/images/turtle.png"),
                        16, 24, 4, 0));
        AssetPool.addSpritesheet("assets/images/bigSpritesheet.png",
                new Spritesheet(AssetPool.getTexture("assets/images/bigSpritesheet.png"),
                        16, 32, 42, 0));
        AssetPool.addSpritesheet("assets/images/pipes.png",
                new Spritesheet(AssetPool.getTexture("assets/images/pipes.png"),
                        32, 32, 4, 0));
        AssetPool.addSpritesheet("assets/images/items.png",
                new Spritesheet(AssetPool.getTexture("assets/images/items.png"),
                        16, 16, 43, 0));
        AssetPool.addSpritesheet("assets/images/gizmos.png",
                new Spritesheet(AssetPool.getTexture("assets/images/gizmos.png"),
                        24, 48, 3, 0));
        AssetPool.getTexture("assets/images/blendImage2.png");

        AssetPool.addSound("assets/sounds/main-theme-overworld.ogg", true);
        AssetPool.addSound("assets/sounds/flagpole.ogg", false);
        AssetPool.addSound("assets/sounds/break_block.ogg", false);
        AssetPool.addSound("assets/sounds/bump.ogg", false);
        AssetPool.addSound("assets/sounds/coin.ogg", false);
        AssetPool.addSound("assets/sounds/gameover.ogg", false);
        AssetPool.addSound("assets/sounds/jump-small.ogg", false);
        AssetPool.addSound("assets/sounds/mario_die.ogg", false);
        AssetPool.addSound("assets/sounds/pipe.ogg", false);
        AssetPool.addSound("assets/sounds/powerup.ogg", false);
        AssetPool.addSound("assets/sounds/powerup_appears.ogg", false);
        AssetPool.addSound("assets/sounds/stage_clear.ogg", false);
        AssetPool.addSound("assets/sounds/stomp.ogg", false);
        AssetPool.addSound("assets/sounds/kick.ogg", false);
        AssetPool.addSound("assets/sounds/invincible.ogg", false);

        AssetPool.getSound(("assets/sounds/main-theme-overworld.ogg")).stop();

        for (GameObject g : scene.getGameObjects()) {
            if (g.getComponent(SpriteRenderer.class) != null) {
                SpriteRenderer spr = g.getComponent(SpriteRenderer.class);
                if (spr.getTexture() != null) {
                    spr.setTexture(AssetPool.getTexture(spr.getTexture().getFilepath()));
                }
            }

            if (g.getComponent(StateMachine.class) != null) {
                StateMachine stateMachine = g.getComponent(StateMachine.class);
                stateMachine.refreshTextures();
            }
        }
    }

    @Override
    public void imgui() {
        ImGui.begin("Level Editor Stuff");
        levelEditorStuff.imgui();
        ImGui.end();

        ImGui.begin("Test window");

        if (ImGui.beginTabBar("WindowTabBar")) {
            if (ImGui.beginTabItem("Solid Blocks")) {
                displaySprites(sprites, 0, 34, 38, 61);
                ImGui.endTabItem();
            }

            if (ImGui.beginTabItem("Decoration Blocks")) {
                displaySprites(sprites, 34, 35, 38, 42, 45, 61);
                ImGui.endTabItem();
            }

            if (ImGui.beginTabItem("Prefabs")) {
                displayPrefabSprites();
                ImGui.endTabItem();
            }

            if (ImGui.beginTabItem("Sounds")) {
                displaySounds();
                ImGui.endTabItem();
            }
            ImGui.endTabBar();
        }

        ImGui.end();
    }

    private void displaySprites(Spritesheet sprites, int start, int... ranges) {
        ImVec2 windowPos = new ImVec2();
        ImGui.getWindowPos(windowPos);
        ImVec2 windowSize = new ImVec2();
        ImGui.getWindowSize(windowSize);
        ImVec2 itemSpacing = new ImVec2();
        ImGui.getStyle().getItemSpacing(itemSpacing);

        float windowX2 = windowPos.x + windowSize.x;
        for (int i = start; i < sprites.size(); i++) {
            boolean skip = false;
            for (int j = 0; j < ranges.length; j += 2) {
                if (i >= ranges[j] && i < ranges[j + 1]) {
                    skip = true;
                    break;
                }
            }
            if (skip) continue;

            Sprite sprite = sprites.getSprite(i);
            float spriteWidth = sprite.getWidth() * 4;
            float spriteHeight = sprite.getHeight() * 4;
            int id = sprite.getTexId();
            Vector2f[] texCoords = sprite.getTexCoords();

            ImGui.pushID(i);
            if (ImGui.button("##button" + i, spriteWidth, spriteHeight)) {
                GameObject object = Prefabs.generateSpriteObject(sprite, 0.25f, 0.25f);
                if (start == 0) {
                    Rigidbody2D rb = new Rigidbody2D();
                    rb.setBodyType(BodyType.Static);
                    object.addComponent(rb);
                    Box2DCollider b2d = new Box2DCollider();
                    b2d.setHalfSize(new Vector2f(0.25f, 0.25f));
                    object.addComponent(b2d);
                    object.addComponent(new Ground());
                    if (i == 12) {
                        object.addComponent(new BreakableBrick());
                    }
                }
                levelEditorStuff.getComponent(MouseControls.class).pickupObject(object);
            }
            ImGui.sameLine();
            ImGui.image(id, spriteWidth, spriteHeight, texCoords[0].x, texCoords[0].y, texCoords[2].x, texCoords[2].y);
            ImGui.popID();

            ImVec2 lastButtonPos = new ImVec2();
            ImGui.getItemRectMax(lastButtonPos);
            float lastButtonX2 = lastButtonPos.x;
            float nextButtonX2 = lastButtonX2 + itemSpacing.x + spriteWidth;
            if (i + 1 < sprites.size() && nextButtonX2 < windowX2) {
                ImGui.sameLine();
            }
        }
    }

    private void displayPrefabSprites() {
        int uid = 0;
        Spritesheet playerSprites = AssetPool.getSpritesheet("assets/images/spritesheet.png");
        Spritesheet items = AssetPool.getSpritesheet("assets/images/items.png");
        Spritesheet turtle = AssetPool.getSpritesheet("assets/images/turtle.png");
        Spritesheet pipes = AssetPool.getSpritesheet("assets/images/pipes.png");

        displayPrefabSprite(playerSprites, 0, uid++, Prefabs::generateMario);
        displayPrefabSprite(items, 0, uid++, Prefabs::generateQuestionBlock);
        displayPrefabSprite(items, 7, uid++, Prefabs::generateCoin);
        displayPrefabSprite(playerSprites, 14, uid++, Prefabs::generateGoomba);
        displayPrefabSprite(turtle, 0, uid++, Prefabs::generateTurtle);
        displayPrefabSprite(items, 6, uid++, Prefabs::generateFlagtop);
        displayPrefabSprite(items, 33, uid++, Prefabs::generateFlagPole);
        displayPrefabSprite(pipes, 0, uid++, () -> Prefabs.generatePipe(Direction.Down));
        displayPrefabSprite(pipes, 1, uid++, () -> Prefabs.generatePipe(Direction.Up));
        displayPrefabSprite(pipes, 2, uid++, () -> Prefabs.generatePipe(Direction.Right));
        displayPrefabSprite(pipes, 3, uid++, () -> Prefabs.generatePipe(Direction.Left));
    }

    private void displayPrefabSprite(Spritesheet spritesheet, int spriteIndex, int uid, Supplier<GameObject> prefabSupplier) {
        Sprite sprite = spritesheet.getSprite(spriteIndex);
        float spriteWidth = sprite.getWidth() * 4;
        float spriteHeight = sprite.getHeight() * 4;
        int id = sprite.getTexId();
        Vector2f[] texCoords = sprite.getTexCoords();

        ImGui.pushID(uid);
        if (ImGui.button("##button" + uid, spriteWidth, spriteHeight)) {
            GameObject object = prefabSupplier.get();
            levelEditorStuff.getComponent(MouseControls.class).pickupObject(object);
        }
        ImGui.sameLine();
        ImGui.image(id, spriteWidth, spriteHeight, texCoords[0].x, texCoords[0].y, texCoords[2].x, texCoords[2].y);
        ImGui.popID();
    }

    private void displaySounds() {
        Collection<Sound> sounds = AssetPool.getAllSounds();
        for (Sound sound : sounds) {
            File tmp = new File(sound.getFilepath());
            if (ImGui.button(tmp.getName())) {
                if (!sound.isPlaying()) {
                    sound.play();
                } else {
                    sound.stop();
                }
            }

            if (ImGui.getContentRegionAvailX() > 100) {
                ImGui.sameLine();
            }
        }
    }
}
