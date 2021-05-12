function createCanvas(idCanvas) {
  const canvas = document.createElement("CANVAS");
  canvas.id = idCanvas;
  canvas.classList.add("graficos");
  const contenedorCanvas = document.createElement("DIV");
  contenedorCanvas.appendChild(canvas);
  $informacion.appendChild(contenedorCanvas);
}

function displayGraphics(labels, values, idCanvas, titulo) {
  let ctx = document.getElementById(idCanvas).getContext("2d");
  let myChart = new Chart(ctx, {
    type: "bar",
    data: {
      labels: labels,
      datasets: [
        {
          label: "#",
          data: values,
          backgroundColor: [
            "rgba(255, 99, 132, 0.2)",
            "rgba(54, 162, 235, 0.2)",
            "rgba(255, 206, 86, 0.2)",
            "rgba(75, 192, 192, 0.2)",
            "rgba(153, 102, 255, 0.2)",
            "rgba(255, 159, 64, 0.2)",
            "rgba(232, 127, 178, 0.2)",
            "rgba(77, 143, 179, 0.2)",
            "rgba(80, 230, 204, 0.2)",
          ],
          borderColor: [
            "rgba(255, 99, 132, 1)",
            "rgba(54, 162, 235, 1)",
            "rgba(255, 206, 86, 1)",
            "rgba(75, 192, 192, 1)",
            "rgba(153, 102, 255, 1)",
            "rgba(255, 159, 64, 1)",
            "rgba(232, 127, 178, 1)",
            "rgba(77, 143, 179, 1)",
            "rgba(80, 230, 204, 1)",
          ],
          borderWidth: 1,
        },
      ],
    },
    options: {
      scales: {
        yAxes: [
          {
            ticks: {
              beginAtZero: true,
            },
          },
        ],
      },
      title: {
        display: true,
        text: titulo,
      },
    },
  });
}
