import { GameObject } from "./GameObject"
import { Wall } from '@/assets/scripts/Wall'
import { Snake } from "./Snake";

export class GameMap extends GameObject {
    constructor(ctx, parent, store) {
        super();
        this.ctx = ctx;
        this.store = store;
        this.parent = parent;
        this.graph = store.state.pk.graph;
        this.rows = store.state.pk.graph.length;
        this.cols = store.state.pk.graph[0].length;
        this.L = 0;
        this.walls = [];
        this.snakes = [
            new Snake({ id: store.state.pk.a_id, color: "#4876EC", r: store.state.pk.a_sx, c: store.state.pk.a_sy }, this),
            new Snake({ id: store.state.pk.b_id, color: "#F94848", r: store.state.pk.b_sx, c: store.state.pk.b_sy }, this),
        ];
    }

    create_walls() {
        for (let r = 0; r < this.rows; r++) {
            for (let c = 0; c < this.cols; c++) {
                if (this.graph[r][c] === 1) {
                    this.walls.push(new Wall(r, c, this));
                }
            }
        }
    }

    add_listening_events() {
        if (this.store.state.record.record_loser === 'none' && this.store.state.pk.gameObject === null) {
            window.location.replace("https://www.kingofbots.fun/record/");
        } else if (this.store.state.record.is_record) {
            let k = 0;
            const a_steps = this.store.state.record.a_steps;
            const b_steps = this.store.state.record.b_steps;
            const loser = this.store.state.record.record_loser;
            const [snake0, snake1] = this.snakes;
            const intervalId =  setInterval(() =>{
                if (k >= a_steps.length - 1) {
                    if (loser === 'all' || loser === 'A') {
                        snake0.status = 'die';
                    }
                    if (loser === 'all' || loser === 'B') {
                        snake1.status = 'die';
                    }
                    clearInterval(intervalId)
                } else {
                    snake0.set_direction(parseInt(a_steps[k]));
                    snake1.set_direction(parseInt(b_steps[k]));
                    k++;
                }
            }, 500);
        } else {
            this.ctx.canvas.focus();

            this.ctx.canvas.addEventListener("keydown", e => {
                let d = -1;
                if (e.key === 'w') d = 0;
                else if (e.key === 'd') d = 1;
                else if (e.key === 's') d = 2;
                else if (e.key === 'a') d = 3;
                if (d >= 0) {
                    this.store.state.pk.socket.send(JSON.stringify({
                        event: "move",
                        direction: d,
                    }))
                }
            });
        }
    }

    start() {
        this.create_walls();
        this.add_listening_events();
    }

    update_size() {
        this.L = parseInt(Math.min(this.parent.clientWidth / this.cols, this.parent.clientHeight / this.rows));
        this.ctx.canvas.width = this.L * this.cols;
        this.ctx.canvas.height = this.L * this.rows;
    }

    check_ready() {
        for (const snake of this.snakes) {
            if (snake.status !== "idle") return false;
            if (snake.direction === -1) return false;
        }

        return true;
    }

    next_step() {
        for (const snake of this.snakes) {
            snake.next_step();
        }
    }

    update() {
        this.update_size();
        if (this.check_ready()) {
            this.next_step();
        }
        this.render();
    }

    render() {
        const color_even = '#AAD751', color_odd = '#A2D149';
        for (let r = 0; r < this.rows; r++) {
            for (let c = 0; c < this.cols; c++) {
                if ((r + c) % 2 === 0) {
                    this.ctx.fillStyle = color_even;
                } else {
                    this.ctx.fillStyle = color_odd;
                }
                this.ctx.fillRect(c * this.L, r * this.L, this.L, this.L);
            }
        }
    }
}