extends Node3D

var core: MeshInstance3D
var ring_a: MeshInstance3D
var ring_b: MeshInstance3D
var ring_c: MeshInstance3D
var camera: Camera3D
var glow: OmniLight3D
var metric_label: Label
var status_label: Label
var selected_zone := "المحور"
var pulse := 0.0
var dragging := false
var last_touch := Vector2.ZERO
var camera_yaw := 0.0
var camera_pitch := -0.08

var zones = [
    {"name":"السوق", "pos":Vector3(-3.6, 1.7, 0.4), "color":Color("35D6FF")},
    {"name":"الرسم البياني", "pos":Vector3(3.6, 1.7, 0.4), "color":Color("FFC857")},
    {"name":"البوت", "pos":Vector3(-3.9, -1.8, 0.6), "color":Color("62E6A7")},
    {"name":"المخاطر", "pos":Vector3(3.9, -1.8, 0.6), "color":Color("FF6B8A")},
    {"name":"التحليل", "pos":Vector3(0, -3.1, 0.9), "color":Color("9A7CFF")}
]

func _ready() -> void:
    _build_world()
    _build_hud()

func _build_world() -> void:
    camera = Camera3D.new()
    camera.position = Vector3(0, 0.8, 13.5)
    add_child(camera)
    camera.look_at(Vector3.ZERO)

    var environment_node := WorldEnvironment.new()
    var environment := Environment.new()
    environment.background_mode = Environment.BG_COLOR
    environment.background_color = Color("07121F")
    environment.ambient_light_source = Environment.AMBIENT_SOURCE_COLOR
    environment.ambient_light_color = Color("A6C9D9")
    environment.ambient_light_energy = 0.42
    environment.tonemap_mode = Environment.TONE_MAPPER_ACES
    environment_node.environment = environment
    add_child(environment_node)

    var key := DirectionalLight3D.new()
    key.rotation_degrees = Vector3(-38, -25, 0)
    key.light_color = Color("D8F5FF")
    key.light_energy = 1.25
    add_child(key)

    glow = OmniLight3D.new()
    glow.light_color = Color("38D8FF")
    glow.light_energy = 7.0
    glow.omni_range = 9.0
    glow.position = Vector3(0, 0, 2)
    add_child(glow)

    core = MeshInstance3D.new()
    var sphere := SphereMesh.new()
    sphere.radius = 1.65
    sphere.height = 3.3
    sphere.radial_segments = 96
    sphere.rings = 64
    core.mesh = sphere
    core.material_override = _material(Color("0F9FC7"), 0.35, 0.2)
    add_child(core)

    ring_a = _ring(2.35, 0.045, Color("FFC857"))
    ring_a.rotation_degrees = Vector3(66, 0, 0)
    ring_b = _ring(2.75, 0.028, Color("35D6FF"))
    ring_b.rotation_degrees = Vector3(74, 22, 0)
    ring_c = _ring(3.15, 0.018, Color("9A7CFF"))
    ring_c.rotation_degrees = Vector3(18, 52, 18)

    for zone in zones:
        _add_zone(zone["name"], zone["pos"], zone["color"])

func _ring(radius: float, tube: float, color: Color) -> MeshInstance3D:
    var mesh_instance := MeshInstance3D.new()
    var torus := TorusMesh.new()
    torus.inner_radius = radius - tube
    torus.outer_radius = radius + tube
    torus.rings = 96
    torus.ring_segments = 12
    mesh_instance.mesh = torus
    mesh_instance.material_override = _material(color, 0.18, 0.0)
    add_child(mesh_instance)
    return mesh_instance

func _add_zone(title: String, position: Vector3, color: Color) -> void:
    var area := Area3D.new()
    area.position = position
    area.name = title
    area.set_meta("title", title)
    area.input_ray_pickable = true
    var collision := CollisionShape3D.new()
    var shape := SphereShape3D.new()
    shape.radius = 0.8
    collision.shape = shape
    area.add_child(collision)
    area.input_event.connect(_zone_input.bind(area))
    add_child(area)

    var mesh_instance := MeshInstance3D.new()
    var sphere := SphereMesh.new()
    sphere.radius = 0.62
    sphere.height = 1.24
    sphere.radial_segments = 48
    sphere.rings = 24
    mesh_instance.mesh = sphere
    mesh_instance.material_override = _material(color, 0.25, 0.05)
    area.add_child(mesh_instance)

    var label := Label3D.new()
    label.text = title
    label.font_size = 30
    label.outline_size = 8
    label.modulate = Color.WHITE
    label.position = Vector3(0, -1.0, 0)
    label.billboard = BaseMaterial3D.BILLBOARD_ENABLED
    area.add_child(label)

func _material(color: Color, emission_strength: float, metallic: float) -> StandardMaterial3D:
    var material := StandardMaterial3D.new()
    material.albedo_color = color
    material.metallic = metallic
    material.roughness = 0.22
    material.emission_enabled = true
    material.emission = color
    material.emission_energy_multiplier = 1.0 + emission_strength * 4.0
    return material

func _build_hud() -> void:
    var layer := CanvasLayer.new()
    layer.layer = 10
    add_child(layer)

    var root := Control.new()
    root.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
    layer.add_child(root)

    var top := ColorRect.new()
    top.color = Color(0.02, 0.07, 0.12, 0.72)
    top.position = Vector2(28, 30)
    top.size = Vector2(484, 116)
    root.add_child(top)

    status_label = Label.new()
    status_label.text = "●  عمار حي  ·  الحساب تجريبي"
    status_label.position = Vector2(26, 18)
    status_label.add_theme_font_size_override("font_size", 28)
    status_label.modulate = Color("62E6A7")
    top.add_child(status_label)

    var title := Label.new()
    title.text = "المحور الحي"
    title.position = Vector2(26, 60)
    title.add_theme_font_size_override("font_size", 22)
    title.modulate = Color("D7F7FF")
    top.add_child(title)

    metric_label = Label.new()
    metric_label.text = "XAUUSD   M5    •    0.00%"
    metric_label.position = Vector2(230, 60)
    metric_label.add_theme_font_size_override("font_size", 18)
    metric_label.modulate = Color("FFC857")
    top.add_child(metric_label)

    var footer := Label.new()
    footer.text = "اسحب المشهد  •  المس أي عقدة  •  المحور يستجيب"
    footer.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
    footer.position = Vector2(20, 1830)
    footer.size = Vector2(500, 50)
    footer.add_theme_font_size_override("font_size", 18)
    footer.modulate = Color(0.78, 0.9, 0.94, 0.82)
    root.add_child(footer)

func _process(delta: float) -> void:
    pulse += delta
    core.rotation.y += delta * 0.16
    core.rotation.x = sin(pulse * 0.55) * 0.08
    ring_a.rotation.z += delta * 0.22
    ring_b.rotation.y -= delta * 0.18
    ring_c.rotation.x += delta * 0.13
    var breathe := 1.0 + sin(pulse * 1.6) * 0.055
    core.scale = Vector3.ONE * breathe
    glow.light_energy = 6.0 + sin(pulse * 1.8) * 1.8
    camera.position.x = lerp(camera.position.x, sin(camera_yaw) * 1.5, delta * 1.8)
    camera.position.y = lerp(camera.position.y, 0.8 + camera_pitch * 2.2, delta * 1.8)
    camera.look_at(Vector3.ZERO)
    metric_label.text = "XAUUSD   M5    •    %0.2f%%" % (sin(pulse * 0.42) * 2.4)

func _zone_input(_camera: Node, event: InputEvent, _position: Vector3, _normal: Vector3, _shape_idx: int, area: Area3D) -> void:
    if event is InputEventScreenTouch and event.pressed:
        selected_zone = area.get_meta("title", "المحور")
        status_label.text = "●  عمار حي  ·  " + selected_zone

func _input(event: InputEvent) -> void:
    if event is InputEventScreenTouch:
        if event.pressed:
            dragging = true
            last_touch = event.position
        else:
            dragging = false
    elif event is InputEventScreenDrag and dragging:
        var delta := event.position - last_touch
        last_touch = event.position
        camera_yaw = clamp(camera_yaw - delta.x * 0.004, -0.8, 0.8)
        camera_pitch = clamp(camera_pitch + delta.y * 0.0025, -0.45, 0.45)
