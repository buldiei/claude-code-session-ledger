<script>
  import '../app.css';
  let { children } = $props();
</script>

<div class="container">
  <a class="brand" href="/">
    <span class="dot"></span>
    <h1>Session Ledger</h1>
  </a>
  {@render children()}
</div>
