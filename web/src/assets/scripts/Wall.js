import { GameObject } from "./GameObject";

export class Wall extends GameObject {
    constructor(r, c, gamemap) {
        super();
        this.r = r;
        this.c = c;
        this.gamemap = gamemap;
        this.color = "#B37226";
    }

    start() {

    }

    update() {
        this.render();
    }

    render() {
        const ctx = this.gamemap.ctx;
        const L = this.gamemap.L;
        ctx.fillStyle = this.color;
        ctx.fillRect(this.c * L, this.r * L, L, L);
    }
}