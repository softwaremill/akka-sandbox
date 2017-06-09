//status count
{
    "requests": [
    {
        "q": "avg:kamon.http_server.200{*}.as_count()",
        "type": "area",
        "conditional_formats": []
    },
    {
        "q": "avg:kamon.http_server.201{*}.as_count()",
        "type": "area"
    },
    {
        "q": "avg:kamon.http_server.202{*}.as_count()",
        "type": "area"
    },
    {
        "q": "avg:kamon.http_server.304{*}.as_count()",
        "type": "area"
    }
],
    "viz": "timeseries",
    "autoscale": true
}

//response time
{
    "viz": "timeseries",
    "status": "done",
    "requests": [
    {
        "q": "avg:kamon.trace.elapsed_time.95percentile{*}",
        "aggregator": "avg",
        "conditional_formats": [],
        "type": "line"
    },
    {
        "q": "avg:kamon.trace.elapsed_time.avg{*}",
        "type": "line"
    },
    {
        "q": "avg:kamon.trace.elapsed_time.max{*}",
        "type": "line"
    },
    {
        "q": "avg:kamon.trace.elapsed_time.median{*}",
        "type": "line"
    }
],
    "autoscale": true
}

//routes
{
    "viz": "timeseries",
    "status": "done",
    "requests": [
    {
        "q": "avg:kamon.trace.elapsed_time.95percentile{trace:get-user} by {host} / 1000000",
        "aggregator": "avg",
        "conditional_formats": [],
        "type": "line"
    },
    {
        "q": "avg:kamon.trace.elapsed_time.95percentile{trace:user-creation} by {host} / 1000000",
        "type": "line"
    }
],
    "autoscale": true
}

//user-creation trace
{
    "viz": "timeseries",
    "status": "done",
    "requests": [
    {
        "q": "avg:kamon.trace_segment.elapsed_time.95percentile{trace:user-creation} by {trace-segment,host} / 1000000",
        "aggregator": "avg",
        "conditional_formats": [],
        "type": "line"
    }
],
    "autoscale": true
}

