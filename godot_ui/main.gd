extends Node3D

# AMAR Living UI 0.1.2 — bright Arabic 3D demo surface; live trading remains disabled.

var core: MeshInstance3D
var ring_a: MeshInstance3D
var ring_b: MeshInstance3D
var ring_c: MeshInstance3D
var camera: Camera3D
var glow: OmniLight3D
var metric_label: Label
var status_label: Label
var title_label: Label
var selected_zone: String = "المحور"
var pulse: float = 0.0
var dragging: bool = false
var last_touch: Vector2 = Vector2.ZERO
var camera_yaw: float = 0.0
var camera_pitch: float = -0.08
var zone_nodes: Array[Area3D] = []

var zones: Array[Dictionary] = [
    {"name":"السوق", "pos":Vector3(-3.8, 1.8, 0.5), "color":Color("19BFEA")},
    {"name":"الرسم البياني", "pos":Vector3(3.8, 1.8, 0.5), "color":Color("E7A92B")},
    {"name":"البوت", "pos":Vector3(-4.0, -1.8, 0.7), "color":Color("24C98A")},
    {"name":"المخاطر", "pos":Vector3(4.0, -1.8, 0.7), "color":Color("E65B76")},
    {"name":"التحليل", "pos":Vector3(0, -3.25, 1.0), "color":Color("8068E8")}
]

func _ready() -> void:
    _build_world()
    _build_hud()

func _build_world() -> void:
    camera = Camera3D.new()
    camera.position = Vector3(0, 0.7, 13.5)
    camera.fov = 46.0
    add_child(camera)
    camera.look_at(Vector3.ZERO)

    var environment_node := WorldEnvironment.new()
    var environment := Environment.new()
    environment.background_mode = Environment.BG_COLOR
    environment.background_color = Color("DDF6FB")
    environment.ambient_light_source = Environment.AMBIENT_SOURCE_COLOR
    environment.ambient_light_color = Color("FFFFFF")
    environment.ambient_light_energy = 1.05
    environment.tonemap_mode = Environment.TONE_MAPPER_ACES
    environment_node.environment = environment
    add_child(environment_node)

    var key := DirectionalLight3D.new()
    key.rotation_degrees = Vector3(-42, -25, 0)
    key.light_color = Color("FFFFFF")
    key.light_energy = 1.65
    add_child(key)

    glow = OmniLight3D.new()
    glow.light_color = Color("27C9E8")
    glow.light_energy = 5.5
    glow.omni_range = 10.0
    glow.position = Vector3(0, 0, 2)
    add_child(glow)

    core = MeshInstance3D.new()
    var sphere := SphereMesh.new()
    sphere.radius = 1.65
    sphere.height = 3.3
    sphere.radial_segments = 96
    sphere.rings = 64
    core.mesh = sphere
    core.material_override = _material(Color("31C9E8"), 0.42, 0.22)
    add_child(core)

    ring_a = _ring(2.35, 0.045, Color("E7A92B"))
    ring_a.rotation_degrees = Vector3(66, 0, 0)
    ring_b = _ring(2.75, 0.028, Color("19BFEA"))
    ring_b.rotation_degrees = Vector3(74, 22, 0)
    ring_c = _ring(3.15, 0.018, Color("8068E8"))
    ring_c.rotation_degrees = Vector3(18, 52, 18)

    for zone: Dictionary in zones:
        _add_zone(String(zone["name"]), zone["pos"] as Vector3, zone["color"] as Color)

func _ring(radius: float, tube: float, color: Color) -> MeshInstance3D:
    var mesh_instance := MeshInstance3D.new()
    var torus := TorusMesh.new()
    torus.inner_radius = radius - tube
    torus.outer_radius = radius + tube
    torus.rings = 96
    torus.ring_segments = 12
    mesh_instance.mesh = torus
    mesh_instance.material_override = _material(color, 0.2, 0.05)
    add_child(mesh_instance)
    return mesh_instance

func _add_zone(title: String, position: Vector3, color: Color) -> void:
    var area := Area3D.new()
    area.position = position
    area.name = title
    area.set_meta("title", title)
    area.set_meta("base_color", color)
    area.input_ray_pickable = true

    var collision := CollisionShape3D.new()
    var shape := SphereShape3D.new()
    shape.radius = 0.82
    collision.shape = shape
    area.add_child(collision)
    area.input_event.connect(_zone_input.bind(area))
    add_child(area)
    zone_nodes.append(area)

    var mesh_instance := MeshInstance3D.new()
    var sphere := SphereMesh.new()
    sphere.radius = 0.66
    sphere.height = 1.32
    sphere.radial_segments = 48
    sphere.rings = 24
    mesh_instance.mesh = sphere
    mesh_instance.material_override = _material(color, 0.3, 0.08)
    area.add_child(mesh_instance)
    area.set_meta("mesh", mesh_instance)

    var label := Label3D.new()
    label.text = title
    label.font_size = 30
    label.outline_size = 8
    label.modulate = Color("18324A")
    label.position = Vector3(0, -1.05, 0)
    label.billboard = BaseMaterial3D.BILLBOARD_ENABLED
    area.add_child(label)

func _material(color: Color, emission_strength: float, metallic: float) -> StandardMaterial3D:
    var material := StandardMaterial3D.new()
    material.albedo_color = color
    material.metallic = metallic
    material.roughness = 0.2
    material.emission_enabled = true
    material.emission = color
    material.emission_energy_multiplier = 1.0 + emission_strength * 3.0
    return material

func _build_hud() -> void:
    var layer := CanvasLayer.new()
    layer.layer = 20
    add_child(layer)

    var root := Control.new()
    root.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
    root.mouse_filter = Control.MOUSE_FILTER_IGNORE
    layer.add_child(root)

    var top := ColorRect.new()
    top.color = Color(1.0, 1.0, 1.0, 0.78)
    top.set_anchors_preset(Control.PRESET_TOP_WIDE)
    top.position = Vector2(24, 24)
    top.size.y = 150
    root.add_child(top)

    status_label = Label.new()
    status_label.text = "عمار حي  ·  الحساب تجريبي"
    status_label.set_anchors_preset(Control.PRESET_TOP_RIGHT)
    status_label.position = Vector2(-380, 26)
    status_label.size = Vector2(340, 42)
    status_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_RIGHT
    status_label.add_theme_font_size_override("font_size", 26)
    status_label.modulate = Color("159A6B")
    top.add_child(status_label)

    title_label = Label.new()
    title_label.text = "المحور الحي"
    title_label.position = Vector2(28, 22)
    title_label.size = Vector2(500, 52)
    title_label.add_theme_font_size_override("font_size", 34)
    title_label.modulate = Color("18324A")
    top.add_child(title_label)

    metric_label = Label.new()
    metric_label.text = "الذهب  ·  إطار 5 دقائق  ·  +0.00%"
    metric_label.position = Vector2(28, 78)
    metric_label.size = Vector2(700, 42)
    metric_label.add_theme_font_size_override("font_size", 20)
    metric_label.modulate = Color("B47A13")
    top.add_child(metric_label)

    var center_hint := Label.new()
    center_hint.text = "اسحب المشهد  •  المس عقدة  •  عمار يستجيب"
    center_hint.set_anchors_preset(Control.PRESET_CENTER_BOTTOM)
    center_hint.position = Vector2(-300, -120)
    center_hint.size = Vector2(600, 48)
    center_hint.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
    center_hint.add_theme_font_size_override("font_size", 19)
    center_hint.modulate = Color(0.1, 0.2, 0.3, 0.72)
    root.add_child(center_hint)

    var nav := ColorRect.new()
    nav.color = Color(1.0, 1.0, 1.0, 0.84)
    nav.set_anchors_preset(Control.PRESET_BOTTOM_WIDE)
    nav.position.y = -112
    nav.size.y = 88
    root.add_child(nav)

    var nav_text := Label.new()
    nav_text.text = "الرئيسية     السوق     الرسم     البوت     المخاطر"
    nav_text.set_anchors_preset(Control.PRESET_FULL_RECT)
    nav_text.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
    nav_text.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
    nav_text.add_theme_font_size_override("font_size", 18)
    nav_text.modulate = Color("35536A")
    nav.add_child(nav_text)

func _process(delta: float) -> void:
    pulse += delta
    core.rotation.y += delta * 0.16
    core.rotation.x = sin(pulse * 0.55) * 0.08
    ring_a.rotation.z += delta * 0.22
    ring_b.rotation.y -= delta * 0.18
    ring_c.rotation.x += delta * 0.13

    var breathe: float = 1.0 + sin(pulse * 1.6) * 0.055
    core.scale = Vector3.ONE * breathe
    glow.light_energy = 5.0 + sin(pulse * 1.8) * 1.5

    camera.position.x = lerp(camera.position.x, sin(camera_yaw) * 1.5, delta * 1.8)
    camera.position.y = lerp(camera.position.y, 0.7 + camera_pitch * 2.2, delta * 1.8)
    camera.look_at(Vector3.ZERO)

    metric_label.text = "الذهب  ·  إطار 5 دقائق  ·  %0.2f%%" % (sin(pulse * 0.42) * 2.4)

    for area: Area3D in zone_nodes:
        var mesh_instance := area.get_meta("mesh") as MeshInstance3D
        var base_color := area.get_meta("base_color") as Color
        var selected := String(area.get_meta("title", "")) == selected_zone
        var zone_breathe: float = 1.0 + sin(pulse * 1.8 + area.position.x) * 0.05
        area.scale = Vector3.ONE * (1.08 if selected else 1.0) * zone_breathe
        if mesh_instance != null:
            mesh_instance.material_override = _material(base_color.lightened(0.08) if selected else base_color, 0.45 if selected else 0.3, 0.08)

func _zone_input(_camera: Node, event: InputEvent, _position: Vector3, _normal: Vector3, _shape_idx: int, area: Area3D) -> void:
    if event is InputEventScreenTouch and event.pressed:
        selected_zone = String(area.get_meta("title", "المحور"))
        status_label.text = "عمار حي  ·  " + selected_zone
        title_label.text = selected_zone + "  ·  المحور الحي"

func _input(event: InputEvent) -> void:
    if event is InputEventScreenTouch:
        if event.pressed:
            dragging = true
            last_touch = event.position
        else:
            dragging = false
    elif event is InputEventScreenDrag and dragging:
        var touch_delta: Vector2 = event.position - last_touch
        last_touch = event.position
        camera_yaw = clamp(camera_yaw - touch_delta.x * 0.004, -0.8, 0.8)
        camera_pitch = clamp(camera_pitch + touch_delta.y * 0.0025, -0.45, 0.45)
